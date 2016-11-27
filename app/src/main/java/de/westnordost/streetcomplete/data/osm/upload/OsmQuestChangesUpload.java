package de.westnordost.streetcomplete.data.osm.upload;


import android.content.res.Resources;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmElement;

// TODO test case
public class OsmQuestChangesUpload
{
	private static String TAG = "QuestUpload";

	private final Resources resources;
	private final MapDataDao osmDao;
	private final OsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final ElementGeometryDao elementGeometryDB;
	private final QuestStatisticsDao statisticsDB;

	@Inject public OsmQuestChangesUpload(
			MapDataDao osmDao, OsmQuestDao questDB, MergedElementDao elementDB,
			ElementGeometryDao elementGeometryDB, Resources resources, QuestStatisticsDao statisticsDB)
	{
		this.resources = resources;
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.elementDB = elementDB;
		this.statisticsDB = statisticsDB;
		this.elementGeometryDB = elementGeometryDB;
	}

	public void upload(AtomicBoolean cancelState)
	{
		int commits = 0, obsolete = 0;

		for(OsmQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break; // break so that the unreferenced stuff is deleted still

			Element element = elementDB.get(quest.getElementType(), quest.getElementId());

			Map<String,String> changesetTags = createChangesetTags(quest.getOsmElementQuestType());
			if (uploadQuestChanges(quest, element, changesetTags, false))
			{
				commits++;
			}
			else {
				obsolete++;
			}
		}

		elementGeometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();

		String logMsg = "Comitted " + commits + " changesets";
		if(obsolete > 0)
		{
			logMsg += " but dropped " + obsolete + " changesets because there were conflicts";
		}

		Log.i(TAG, logMsg);
	}

	boolean uploadQuestChanges(OsmQuest quest, Element element, Map<String,String> changesetTags,
									boolean alreadyHandlingConflict)
	{
		// The element can be null if it has been deleted in the meantime (outside this app usually)
		if(element == null)
		{
			questDB.delete(quest.getId());
			Log.v(TAG, "Dropped quest " + getQuestStringForLog(quest) +
					" because the associated element has already been deleted");
			return false;
		}

		StringMapChanges changes = quest.getChanges();
		Map<String,String> elementTags = element.getTags();
		if(elementTags == null)
		{
			((OsmElement) element).setTags(new HashMap<String, String>());
			elementTags = element.getTags();
		}
		if(changes.hasConflictsTo(elementTags))
		{
			questDB.delete(quest.getId());
			Log.v(TAG, "Dropped quest " + getQuestStringForLog(quest) +
					" because there has been a conflict while applying the changes");
			return false;
		}
		changes.applyTo(element.getTags());

		try
		{
			osmDao.updateMap( changesetTags, Collections.singleton(element), null);
			/* A diff handler is not (yet) necessary: The local copy of an OSM element is updated
			 * automatically on conflict. A diff handler would be necessary if elements could be
			 * created or deleted through quests because IDs of elements would then change. */
		}
		catch(OsmConflictException e)
		{
			/* should not be necessary but as a safeguard against a stack overflow in case
			   the OSM server always answers with OsmConflictException (which is then very probably
			   a programming error on our side */
			if(alreadyHandlingConflict)
			{
				throw new RuntimeException(
						"OSM server continues to report a conflict on uploading the changes for " +
						"the quest " + getQuestStringForLog(quest) + ". The local version is " +
						element.getVersion(), e);
			}
			element = updateElementFromServer(quest.getElementType(), quest.getId());
			uploadQuestChanges(quest, element, changesetTags, true);
		}

		questDB.delete(quest.getId());
		statisticsDB.addOne(quest.getType().getClass().getSimpleName());

		return true;
	}

	private static String getQuestStringForLog(OsmQuest quest)
	{
		return quest.getType().getClass().getSimpleName() + " for " +
				quest.getElementType().name().toLowerCase() + " #" + quest.getElementId();
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

	private Map<String,String> createChangesetTags(OsmElementQuestType questType)
	{
		Map<String,String> changesetTags = new HashMap<>();
		int resourceId = questType.getCommitMessageResourceId();
		changesetTags.put("comment", resources.getString(resourceId));
		changesetTags.put("created_by", ApplicationConstants.USER_AGENT);
		changesetTags.put(ApplicationConstants.QUESTTYPE_TAG_KEY, questType.getClass().getSimpleName());
		changesetTags.put("source", "survey");
		return changesetTags;
	}
}
