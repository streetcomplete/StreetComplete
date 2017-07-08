package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetInfo;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.util.SlippyMapMath;

public class OsmQuestChangesUpload
{
	private static String TAG = "QuestUpload";

	private final MapDataDao osmDao;
	private final OsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final ElementGeometryDao elementGeometryDB;
	private final QuestStatisticsDao statisticsDB;
	private final OpenChangesetsDao openChangesetsDB;
	private final ChangesetsDao changesetsDao;
	private final DownloadedTilesDao downloadedTilesDao;
	private final SharedPreferences prefs;

	// The cache is just here so that uploading 500 quests of same quest type does not result in 500 DB requests.
	private Map<OpenChangesetKey, Long> changesetIdsCache = new HashMap<>();

	@Inject public OsmQuestChangesUpload(
			MapDataDao osmDao, OsmQuestDao questDB, MergedElementDao elementDB,
			ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
			OpenChangesetsDao openChangesetsDB, ChangesetsDao changesetsDao,
			DownloadedTilesDao downloadedTilesDao, SharedPreferences prefs)
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
	}

	public synchronized void upload(AtomicBoolean cancelState)
	{
		int commits = 0, obsolete = 0;
		changesetIdsCache = new HashMap<>();

		for(OsmQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break; // break so that the unreferenced stuff is deleted still

			Element element = elementDB.get(quest.getElementType(), quest.getElementId());

			long changesetId = getChangesetIdOrCreate(quest.getOsmElementQuestType(), quest.getChangesSource());
			if (uploadQuestChange(changesetId, quest, element, false, false))
			{
				commits++;
			}
			else
			{
				obsolete++;
			}
		}

		cleanUp();

		String logMsg = "Committed " + commits + " changes";
		if(obsolete > 0)
		{
			logMsg += " but dropped " + obsolete + " changes because there were conflicts";
		}

		Log.i(TAG, logMsg);

		closeOpenChangesets();
	}

	private void cleanUp()
	{
		long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
		int deletedQuests = questDB.deleteAll(QuestStatus.CLOSED, yesterday);
		if(deletedQuests > 0)
		{
			elementGeometryDB.deleteUnreferenced();
			elementDB.deleteUnreferenced();
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
				Log.d(TAG, "Closed changeset #" + info.changesetId + ".");
			}
			catch (OsmConflictException e)
			{
				Log.i(TAG, "Couldn't close changeset #" + info.changesetId + " because it has already been closed.");
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
			closeQuest(quest);
			LatLon questPosition = quest.getGeometry().center;
			Point tile = SlippyMapMath.enclosingTile(questPosition, ApplicationConstants.QUEST_TILE_ZOOM);
			downloadedTilesDao.remove(tile);
			return false;
		}

		try
		{
			osmDao.uploadChanges( changesetId, Collections.singleton(elementWithChangesApplied), null);
			/* A diff handler is not (yet) necessary: The local copy of an OSM element is updated
			 * automatically on conflict. A diff handler would be necessary if elements could be
			 * created or deleted through quests because IDs of elements would then change. */
		}
		catch(OsmConflictException e)
		{
			return handleConflict(changesetId, quest, element, alreadyHandlingElementConflict,
					alreadyHandlingChangesetConflict, e);
		}

		closeQuest(quest);
		statisticsDB.addOne(quest.getType().getClass().getSimpleName());

		return true;
	}

	private void closeQuest(OsmQuest quest)
	{
		quest.setStatus(QuestStatus.CLOSED);
		questDB.update(quest);
	}

	private Element changesApplied(Element element, OsmQuest quest)
	{
		// The element can be null if it has been deleted in the meantime (outside this app usually)
		if(element == null)
		{
			Log.v(TAG, "Dropping quest " + getQuestStringForLog(quest) +
					" because the associated element has already been deleted");
			return null;
		}

		Element copy = copyElement(element);

		StringMapChanges changes = quest.getChanges();
		if(changes.hasConflictsTo(copy.getTags()))
		{
			Log.v(TAG, "Dropping quest " + getQuestStringForLog(quest) +
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
		return uploadQuestChange(changesetId, quest, element, true, alreadyHandlingChangesetConflict);
	}

	private static String getQuestStringForLog(OsmQuest quest)
	{
		return quest.getType().getClass().getSimpleName() + " for " +
				quest.getElementType().name().toLowerCase() + " #" + quest.getElementId();
	}

	private static Element copyElement(Element e)
	{
		if(e == null) return null;
		Map<String,String> tagsCopy = new HashMap<>();
		if(e.getTags() != null) tagsCopy.putAll(e.getTags());

		if(e instanceof Node)
		{
			return new OsmNode(e.getId(), e.getVersion(), ((Node)e).getPosition(), tagsCopy);
		}
		if(e instanceof Way)
		{
			return new OsmWay(e.getId(), e.getVersion(),
					new ArrayList<>(((Way)e).getNodeIds()), tagsCopy);
		}
		if(e instanceof Relation)
		{
			return new OsmRelation(e.getId(), e.getVersion(),
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
