package de.westnordost.streetcomplete.data.osm.upload;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Provider;

import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetInfo;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener;
import de.westnordost.streetcomplete.util.SlippyMapMath;

import static de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF;

public abstract class AOsmQuestChangesetsUpload
{
	private final String TAG = getLogTag();

	private final MapDataDao osmDao;
	private final AOsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final ElementGeometryDao elementGeometryDB;
	private final QuestStatisticsDao statisticsDB;
	private final OpenChangesetsDao openChangesetsDB;
	private final DownloadedTilesDao downloadedTilesDao;
	private final Provider<OsmQuestChangeUpload> osmQuestChangeUploadProvider;
	private final ChangesetAutoCloser changesetAutoCloser;

	private final List<OsmQuest> createdQuests;
	private final List<Long> removedQuestIds;
	private VisibleQuestListener visibleQuestListener;
	private OnUploadedChangeListener uploadedChangeListener;

	// The cache is just here so that uploading 500 quests of same quest type does not result in 500 DB requests.
	private Map<OpenChangesetKey, Long> changesetIdsCache = new HashMap<>();

	public AOsmQuestChangesetsUpload(
		MapDataDao osmDao, AOsmQuestDao questDB, MergedElementDao elementDB,
		ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
		OpenChangesetsDao openChangesetsDB, DownloadedTilesDao downloadedTilesDao,
		Provider<OsmQuestChangeUpload> osmQuestChangeUploadProvider,
		ChangesetAutoCloser changesetAutoCloser)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.elementDB = elementDB;
		this.statisticsDB = statisticsDB;
		this.elementGeometryDB = elementGeometryDB;
		this.openChangesetsDB = openChangesetsDB;
		this.downloadedTilesDao = downloadedTilesDao;
		this.osmQuestChangeUploadProvider = osmQuestChangeUploadProvider;
		this.changesetAutoCloser = changesetAutoCloser;
		createdQuests = new ArrayList<>();
		removedQuestIds = new ArrayList<>();
	}

	public synchronized void setProgressListener(OnUploadedChangeListener uploadedChangeListener)
	{
		this.uploadedChangeListener = uploadedChangeListener;
	}

	public synchronized void setVisibleQuestListener(VisibleQuestListener visibleQuestListener)
	{
		this.visibleQuestListener = visibleQuestListener;
	}

	public synchronized void upload(AtomicBoolean cancelState)
	{
		int commits = 0, obsolete = 0;
		changesetIdsCache = new HashMap<>();
		createdQuests.clear();
		removedQuestIds.clear();

		HashSet<OsmElementQuestType> uploadedQuestTypes = new HashSet<>();

		for(OsmQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break; // break so that the unreferenced stuff is deleted still

			// was deleted while trying to upload another quest
			if(removedQuestIds.contains(quest.getId())) continue;

			long changesetId = getChangesetIdOrCreate(quest.getOsmElementQuestType(), quest.getChangesSource());
			OsmQuestChangeUpload.UploadResult uploadResult = uploadAndHandleChangesetConflict(changesetId, quest);
			createdQuests.addAll(uploadResult.createdQuests);
			removedQuestIds.addAll(uploadResult.removedQuestIds);
			if (uploadResult.success)
			{
				uploadedQuestTypes.add(quest.getOsmElementQuestType());
				if(uploadedChangeListener != null) uploadedChangeListener.onUploaded();
				statisticsDB.addOne(quest.getType().getClass().getSimpleName());
				commits++;
			}
			else
			{
				if(uploadedChangeListener != null) uploadedChangeListener.onDiscarded();
				obsolete++;
				invalidateAreaAroundQuest(quest);
			}
		}

		cleanUp(uploadedQuestTypes);

		String logMsg = "Committed " + commits + " changes";
		if(obsolete > 0)
		{
			logMsg += " but dropped " + obsolete + " changes because there were conflicts";
		}

		Log.i(TAG, logMsg);

		if(!createdQuests.isEmpty())
		{
			int createdQuestsCount = createdQuests.size();
			if(visibleQuestListener != null)
			{
				visibleQuestListener.onQuestsCreated(createdQuests, QuestGroup.OSM);
			}
			Log.i(TAG, "Created " + createdQuestsCount + " new quests");
		}
		if(!removedQuestIds.isEmpty())
		{
			int removedQuestsCount = removedQuestIds.size();
			if(visibleQuestListener != null)
			{
				visibleQuestListener.onQuestsRemoved(removedQuestIds, QuestGroup.OSM);
			}
			Log.i(TAG, "Removed " + removedQuestsCount + " quests which are no longer applicable");
		}

		closeOpenChangesets();

		if(commits > 0)
		{
			changesetAutoCloser.enqueue();
		}
	}

	private OsmQuestChangeUpload.UploadResult uploadAndHandleChangesetConflict(long changesetId, OsmQuest quest)
	{
		try
		{
			return osmQuestChangeUploadProvider.get().upload(changesetId, quest, shouldCheckForQuestApplicability());
		}
		catch (OsmConflictException e)
		{
			OsmElementQuestType questType = quest.getOsmElementQuestType();

			long newChangesetId = createChangeset(questType, quest.getChangesSource());
			OpenChangesetKey key = new OpenChangesetKey(questType.getClass().getSimpleName(), quest.getChangesSource());
			changesetIdsCache.put(key, newChangesetId);

			// try again with new created changeset. If this still throws an exception, it is
			// likely a programming error in this code
			return osmQuestChangeUploadProvider.get().upload(newChangesetId, quest, shouldCheckForQuestApplicability());
		}
	}

	protected abstract String getLogTag();

	private void cleanUp(Set<OsmElementQuestType> questTypes)
	{
		long timestamp = System.currentTimeMillis() - ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE;
		int deletedQuests = questDB.deleteAllClosed(timestamp);
		if(deletedQuests > 0)
		{
			elementGeometryDB.deleteUnreferenced();
			elementDB.deleteUnreferenced();
			// must be after unreferenced elements have been deleted
			for (OsmElementQuestType questType : questTypes)
			{
				questType.cleanMetadata();
			}
		}
	}

	public synchronized void closeOpenChangesets()
	{
		long timePassed = System.currentTimeMillis() - openChangesetsDB.getLastQuestSolvedTime();
		if(timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return;

		for (OpenChangesetInfo info : openChangesetsDB.getAll())
		{
			try
			{
				osmDao.closeChangeset(info.changesetId);
				Log.i(TAG, "Closed changeset #" + info.changesetId + ".");
			}
			catch (OsmConflictException e)
			{
				Log.w(TAG, "Couldn't close changeset #" + info.changesetId + " because it has already been closed.");
			}
			finally
			{
				// done!
				openChangesetsDB.delete(info.key);
			}
		}
	}

	private long getChangesetIdOrCreate(OsmElementQuestType questType, String source)
	{
		String questTypeName = questType.getClass().getSimpleName();

		OpenChangesetKey key = new OpenChangesetKey(questTypeName, source);
		Long cachedChangesetId = changesetIdsCache.get(key);
		if(cachedChangesetId != null) return cachedChangesetId;

		OpenChangesetInfo changesetInfo = openChangesetsDB.get(key);
		long result;
		if (changesetInfo != null && changesetInfo.changesetId != null)
		{
			result = changesetInfo.changesetId;
		}
		else
		{
			result = createChangeset(questType, source);
		}

		changesetIdsCache.put(key, result);
		return result;
	}

	private long createChangeset(OsmElementQuestType questType, String source)
	{
		OpenChangesetKey key = new OpenChangesetKey(questType.getClass().getSimpleName(), source);
		long changesetId = osmDao.openChangeset(createChangesetTags(questType, source));
		openChangesetsDB.replace(key, changesetId);
		return changesetId;
	}

	private void invalidateAreaAroundQuest(OsmQuest quest)
	{
		// called after a conflict. If there is a conflict, the user is not the only one in that
		// area, so best invalidate all downloaded quests here and redownload on next occasion
		LatLon questPosition = quest.getGeometry().center;
		Point tile = SlippyMapMath.enclosingTile(questPosition, ApplicationConstants.QUEST_TILE_ZOOM);
		downloadedTilesDao.remove(tile);
	}

	protected abstract boolean shouldCheckForQuestApplicability();

	private Map<String,String> createChangesetTags(OsmElementQuestType questType, String source)
	{
		Map<String,String> changesetTags = new HashMap<>();
		String commitMessage = questType.getCommitMessage();
		changesetTags.put("comment", commitMessage);
		changesetTags.put("created_by", ApplicationConstants.USER_AGENT);
		String questTypeName = questType.getClass().getSimpleName();
		changesetTags.put(ApplicationConstants.QUESTTYPE_TAG_KEY, questTypeName);
		changesetTags.put("source", source);
		return changesetTags;
	}
}
