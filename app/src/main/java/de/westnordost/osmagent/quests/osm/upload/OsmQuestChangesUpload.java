package de.westnordost.osmagent.quests.osm.upload;


import android.content.res.Resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.changes.StringMapChanges;
import de.westnordost.osmagent.quests.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.quests.osm.persist.MergedElementDao;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.quests.statistics.QuestStatisticsDao;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmElement;

// TODO test case
public class OsmQuestChangesUpload
{
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
		for(OsmQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break; // break so that the unreferenced stuff is deleted still

			Element element = elementDB.get(quest.getElementType(), quest.getElementId());

			Map<String,String> changesetTags = createChangesetTags(quest.getOsmElementQuestType());
			boolean success = uploadQuestChanges(quest, element, changesetTags, false);

			if (success)
			{
				statisticsDB.addOne(quest.getType().getClass().getSimpleName());
			}

			questDB.delete(quest.getId());
		}

		elementGeometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();
	}

	private boolean uploadQuestChanges(
			OsmQuest quest, Element element, Map<String,String> changesetTags,
			boolean alreadyHandlingConflict)
	{
		// The element can be null if it has been deleted in the meantime (outside this app usually)
		if(element == null)
		{
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
						"OSM server continues to report a conflict on uploading the " +
						element.getType().name().toLowerCase() + " " + element.getId() + ". The " +
						"local version is " + element.getVersion(), e);
			}
			element = updateElementFromServer(quest.getElementType(), quest.getId());
			uploadQuestChanges(quest, element, changesetTags, true);
		}
		return true;
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
		changesetTags.put("created_by", OsmagentConstants.USER_AGENT);
		changesetTags.put(OsmagentConstants.QUESTTYPE_TAG_KEY, questType.getClass().getSimpleName());
		changesetTags.put("source", "survey");
		return changesetTags;
	}
}
