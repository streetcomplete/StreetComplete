package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetInfo;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener;
import de.westnordost.streetcomplete.util.SlippyMapMath;

public abstract class AOsmQuestChangesUpload
{
	private final String TAG = getLogTag();

	private final MapDataDao osmDao;
	private final AOsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final ElementGeometryDao elementGeometryDB;
	private final QuestStatisticsDao statisticsDB;
	private final OpenChangesetsDao openChangesetsDB;
	private final ChangesetsDao changesetsDao;
	private final DownloadedTilesDao downloadedTilesDao;
	private final SharedPreferences prefs;
	private final OsmQuestGiver questUnlocker;

	private final List<OsmQuest> createdQuests;
	private final List<Long> removedQuestIds;
	private VisibleQuestListener visibleQuestListener;
	private OnUploadedChangeListener uploadedChangeListener;

	// The cache is just here so that uploading 500 quests of same quest type does not result in 500 DB requests.
	private Map<OpenChangesetKey, Long> changesetIdsCache = new HashMap<>();

	public AOsmQuestChangesUpload(
			MapDataDao osmDao, AOsmQuestDao questDB, MergedElementDao elementDB,
			ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
			OpenChangesetsDao openChangesetsDB, ChangesetsDao changesetsDao,
			DownloadedTilesDao downloadedTilesDao, SharedPreferences prefs,
			OsmQuestGiver questUnlocker)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.elementDB = elementDB;
		this.statisticsDB = statisticsDB;
		this.elementGeometryDB = elementGeometryDB;
		this.openChangesetsDB = openChangesetsDB;
		this.changesetsDao = changesetsDao;
		this.downloadedTilesDao = downloadedTilesDao;
		this.prefs = prefs;
		this.questUnlocker = questUnlocker;
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

			Element element = elementDB.get(quest.getElementType(), quest.getElementId());

			long changesetId = getChangesetIdOrCreate(quest.getOsmElementQuestType(), quest.getChangesSource());
			if (uploadQuestChange(changesetId, quest, element, false, false))
			{
				uploadedQuestTypes.add(quest.getOsmElementQuestType());
				uploadedChangeListener.onUploaded();
				commits++;
			}
			else
			{
				uploadedChangeListener.onDiscarded();
				obsolete++;
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
			// changesets are closed delayed after X minutes of inactivity
			ChangesetAutoCloserJob.scheduleJob();
		}
	}

	protected abstract String getLogTag();

	private void cleanUp(Set<OsmElementQuestType> questTypes)
	{
		long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
		int deletedQuests = questDB.deleteAllClosed(yesterday);
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
		if(timePassed < OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return;

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

	boolean uploadQuestChange(long changesetId, OsmQuest quest, Element element,
							  boolean alreadyHandlingElementConflict,
							  boolean alreadyHandlingChangesetConflict)
	{
		Element elementWithChangesApplied = changesApplied(element, quest);
		if(elementWithChangesApplied == null)
		{
			deleteConflictingQuest(quest);
			return false;
		}

		int[] newVersion = {element.getVersion()};
		try
		{
			osmDao.uploadChanges(changesetId, Collections.singleton(elementWithChangesApplied),	diffElement ->
			{
				if(diffElement.clientId == elementWithChangesApplied.getId())
				{
					newVersion[0] = diffElement.serverVersion;
					/* It is not necessary (yet) to handle updating the element's id because
					   StreetComplete does not add or delete elements */
				}
			});
		}
		catch(OsmConflictException e)
		{
			return handleConflict(changesetId, quest, element, alreadyHandlingElementConflict,
					alreadyHandlingChangesetConflict, e);
		}
		Element updatedElement = copyElement(elementWithChangesApplied, newVersion[0]);

		closeQuest(quest);
		// save with new version when persisting to DB
		elementDB.put(updatedElement);
		statisticsDB.addOne(quest.getType().getClass().getSimpleName());

		OsmQuestGiver.QuestUpdates questUpdates = questUnlocker.updateQuests(updatedElement);
		createdQuests.addAll(questUpdates.createdQuests);
		removedQuestIds.addAll(questUpdates.removedQuestIds);

		return true;
	}

	private void closeQuest(OsmQuest quest)
	{
		quest.setStatus(QuestStatus.CLOSED);
		questDB.update(quest);
	}

	private void deleteConflictingQuest(OsmQuest quest)
	{
		// #812 conflicting quests may not reside in the database, otherwise they would wrongfully
		//      be candidates for an undo - even though nothing was changed
		questDB.delete(quest.getId());
		invalidateAreaAroundQuest(quest);
	}

	private void invalidateAreaAroundQuest(OsmQuest quest)
	{
		// called after a conflict. If there is a conflict, the user is not the only one in that
		// area, so best invalidate all downloaded quests here and redownload on next occasion
		LatLon questPosition = quest.getGeometry().center;
		Point tile = SlippyMapMath.enclosingTile(questPosition, ApplicationConstants.QUEST_TILE_ZOOM);
		downloadedTilesDao.remove(tile);
	}

	private Element changesApplied(Element element, OsmQuest quest)
	{
		// The element can be null if it has been deleted in the meantime (outside this app usually)
		if(element == null)
		{
			Log.d(TAG, "Dropping quest " + getQuestStringForLog(quest) +
					" because the associated element has already been deleted");
			return null;
		}

		Element copy = copyElement(element, element.getVersion());

		StringMapChanges changes = quest.getChanges();
		if(changes.hasConflictsTo(copy.getTags()))
		{
			Log.d(TAG, "Dropping quest " + getQuestStringForLog(quest) +
					" because there has been a conflict while applying the changes");
			return null;
		}
		changes.applyTo(copy.getTags());
		return copy;
	}

	private boolean handleConflict(long changesetId, OsmQuest quest, Element element,
								   boolean alreadyHandlingElementConflict,
								   boolean alreadyHandlingChangesetConflict, OsmConflictException e)
	{
		/* Conflict can either happen because of the changeset or because of the element(s) uploaded.
		   Let's find out. */

		ChangesetInfo changesetInfo = changesetsDao.get(changesetId);

		Long myUserId = prefs.getLong(Prefs.OSM_USER_ID, -1);
		// can happen if the user changes his OAuth identity in the settings while having an open changeset
		boolean changesetWasOpenedByDifferentUser =
				myUserId == -1 || changesetInfo.user == null || changesetInfo.user.id != myUserId;

		if(!changesetInfo.isOpen || changesetWasOpenedByDifferentUser)
		{
			// safeguard against stack overflow in case of programming error
			if(alreadyHandlingChangesetConflict)
			{
				throw new RuntimeException("OSM server continues to report a changeset " +
						"conflict for changeset id " + changesetId, e);
			}
			return handleChangesetConflict(quest, element, alreadyHandlingElementConflict);
		}
		else
		{
			// safeguard against stack overflow in case of programming error
			if(alreadyHandlingElementConflict)
			{
				throw new RuntimeException("OSM server continues to report an element " +
						"conflict on uploading the changes for the quest " +
						getQuestStringForLog(quest) + ". The local version is " +
						element.getVersion(), e);
			}
			return handleElementConflict(changesetId, quest, alreadyHandlingChangesetConflict);
		}
	}

	private boolean handleChangesetConflict(OsmQuest quest, Element element,
											boolean alreadyHandlingElementConflict)
	{
		OsmElementQuestType questType = quest.getOsmElementQuestType();

		long changesetId = createChangeset(questType, quest.getChangesSource());
		OpenChangesetKey key = new OpenChangesetKey(questType.getClass().getSimpleName(), quest.getChangesSource());
		changesetIdsCache.put(key, changesetId);

		return uploadQuestChange(changesetId, quest, element, alreadyHandlingElementConflict, true);
	}

	private boolean handleElementConflict(long changesetId, OsmQuest quest,
										  boolean alreadyHandlingChangesetConflict)
	{
		Element element = updateElementFromServer(quest.getElementType(), quest.getElementId());
		// if after updating to the new version of the element, the quest is not applicable to the
		// element anymore, drop it (#720)
		if(element != null)
		{
			if (!questIsApplicableToElement(quest, element))
			{
				Log.d(TAG, "Dropping quest " + getQuestStringForLog(quest) +
					" because the quest is no longer applicable to the element");
				deleteConflictingQuest(quest);
				return false;
			}
		}

		return uploadQuestChange(changesetId, quest, element, true, alreadyHandlingChangesetConflict);
	}

	protected abstract boolean questIsApplicableToElement(OsmQuest quest, Element element);

	private static String getQuestStringForLog(OsmQuest quest)
	{
		return quest.getType().getClass().getSimpleName() + " for " +
				quest.getElementType().name().toLowerCase() + " #" + quest.getElementId();
	}

	private static Element copyElement(Element e, int newVersion)
	{
		if(e == null) return null;
		Map<String,String> tagsCopy = new HashMap<>();
		if(e.getTags() != null) tagsCopy.putAll(e.getTags());

		if(e instanceof Node)
		{
			return new OsmNode(e.getId(), newVersion, ((Node)e).getPosition(), tagsCopy);
		}
		if(e instanceof Way)
		{
			return new OsmWay(e.getId(), newVersion,
					new ArrayList<>(((Way)e).getNodeIds()), tagsCopy);
		}
		if(e instanceof Relation)
		{
			return new OsmRelation(e.getId(), newVersion,
					new ArrayList<>(((Relation)e).getMembers()), tagsCopy);
		}
		return null;
	}

	private Element updateElementFromServer(Element.Type elementType, long id)
	{
		Element element = null;

		switch(elementType)
		{
			case NODE:
				element = osmDao.getNode(id);
				break;
			case WAY:
				element = osmDao.getWay(id);
				break;
			case RELATION:
				element = osmDao.getRelation(id);
				break;
		}

		if(element != null)
		{
			elementDB.put(element);
		}
		else
		{
			elementDB.delete(elementType, id);
		}

		return element;
	}

	private Map<String,String> createChangesetTags(OsmElementQuestType questType, String source)
	{
		Map<String,String> changesetTags = new HashMap<>();
		String commitMessage = questType.getCommitMessage();
		if(commitMessage != null)
		{
			changesetTags.put("comment", commitMessage);
		}
		changesetTags.put("created_by", ApplicationConstants.USER_AGENT);
		String questTypeName = questType.getClass().getSimpleName();
		changesetTags.put(ApplicationConstants.QUESTTYPE_TAG_KEY, questTypeName);
		changesetTags.put("source", source);
		return changesetTags;
	}
}
