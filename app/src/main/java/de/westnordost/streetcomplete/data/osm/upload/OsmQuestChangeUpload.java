package de.westnordost.streetcomplete.data.osm.upload;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;

public class OsmQuestChangeUpload
{
	private final String TAG = "OsmQuestUpload";

	private final MapDataDao osmDao;
	private final AOsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final OsmQuestGiver questGiver;

	private boolean called = false;
	private OsmQuest quest;
	private long changesetId;
	private boolean shouldCheckForQuestApplicability;

	private List<OsmQuest> createdQuests = new ArrayList<>();
	private List<Long> removedQuestIds = new ArrayList<>();
	private boolean alreadyHandlingElementConflict;


	@Inject public OsmQuestChangeUpload(
		MapDataDao osmDao, AOsmQuestDao questDB, MergedElementDao elementDB,
		OsmQuestGiver questGiver)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.elementDB = elementDB;
		this.questGiver = questGiver;
	}

	public static class UploadResult
	{
		public final boolean success;
		public final List<OsmQuest> createdQuests;
		public final List<Long> removedQuestIds;

		public UploadResult(boolean success, List<OsmQuest> createdQuests, List<Long> removedQuestIds)
		{
			this.success = success;
			this.createdQuests = createdQuests;
			this.removedQuestIds = removedQuestIds;
		}
	}

	public synchronized UploadResult upload(long changesetId, OsmQuest quest)
	{
		return upload(changesetId, quest, true);
	}

	public synchronized UploadResult upload(
		long changesetId, OsmQuest quest, boolean shouldCheckForQuestApplicability)
	{
		if(called) throw new IllegalStateException("This is a single-use object");
		called = true;

		this.quest = quest;
		this.changesetId = changesetId;
		this.shouldCheckForQuestApplicability = shouldCheckForQuestApplicability;
		Element element = elementDB.get(quest.getElementType(), quest.getElementId());

		boolean success = uploadQuestChange(element);
		if(success)
		{
			quest.setStatus(QuestStatus.CLOSED);
			questDB.update(quest);

		} else {
			// #812 conflicting quests may not reside in the database, otherwise they would wrongfully
			//      be candidates for an undo - even though nothing was changed
			questDB.delete(quest.getId());
		}

		return new UploadResult(success, createdQuests, removedQuestIds);
	}

	private boolean uploadQuestChange(Element element)
	{
		Element elementWithChangesApplied = changesApplied(element);
		if(elementWithChangesApplied == null)
		{
			return false;
		}

		int[] newVersion = {element.getVersion()};
		try
		{
			osmDao.uploadChanges(changesetId, Collections.singleton(elementWithChangesApplied), diffElement ->
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
			return handleConflict(element, e);
		}
		Element updatedElement = copyElement(elementWithChangesApplied, newVersion[0]);

		// save with new version when persisting to DB
		putUpdatedElement(updatedElement);

		return true;
	}

	private Element changesApplied(Element element)
	{
		// The element can be null if it has been deleted in the meantime (outside this app usually)
		if(element == null)
		{
			Log.d(TAG, "Dropping quest " + getQuestStringForLog() +
				" because the associated element has already been deleted");
			return null;
		}

		Element copy = copyElement(element, element.getVersion());

		StringMapChanges changes = quest.getChanges();
		try
		{
			changes.applyTo(copy.getTags());
		}
		catch (IllegalStateException e)
		{
			Log.d(TAG, "Dropping quest " + getQuestStringForLog() +
				" because there has been a conflict while applying the changes");
			return null;
		}
		catch (IllegalArgumentException e)
		{
			/* There is a max key/value length limit of 255 characters in OSM. If we reach this
			   point, it means the UI did permit an input of more than that. So, we have to catch
			   this here latest.
			   This is a warning because the UI should prevent this in the first place, at least
			   for free-text input. For structured input, like opening hours, it is another matter
			   because it's awkward to explain to a non-technical user this technical limitation

			   See also https://github.com/openstreetmap/openstreetmap-website/issues/2025
			  */
			Log.w(TAG, "Dropping quest " + getQuestStringForLog() +
				" because a key or value is too long for OSM", e);
			return null;
		}

		return copy;
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


	private boolean handleConflict(Element element, OsmConflictException e)
	{
		// Conflict can either happen because of the changeset or because of the element(s)
		// uploaded. Let's find out.
		Element newElement = getElementFromServer(element.getType(), element.getId());
		if(newElement == null || newElement.getVersion() != element.getVersion())
		{
			// safeguard against stack overflow in case of programming error
			if(alreadyHandlingElementConflict)
			{
				throw new RuntimeException("OSM server continues to report an element " +
					"conflict on uploading the changes for the quest " +
					getQuestStringForLog() + ". The local version is " +
					element.getVersion(), e);
			}
			alreadyHandlingElementConflict = true;

			return handleElementConflict(newElement);
		}
		else
		{
			// a changeset conflict cannot be handled here: throw it along
			throw e;
		}
	}

	private boolean handleElementConflict(Element newElement)
	{
		updateElement(newElement);

		// if after updating to the new version of the element, the quest is not applicable to the
		// element anymore, drop it (#720)
		if(newElement != null)
		{
			if(shouldCheckForQuestApplicability && !questIsApplicableToElement(newElement))
			{
				Log.d(TAG, "Dropping quest " + getQuestStringForLog() +
					" because the quest is no longer applicable to the element");
				return false;
			}
		}

		return uploadQuestChange(newElement);
	}

	private boolean questIsApplicableToElement(Element element)
	{
		Boolean questIsApplicableToElement = quest.getOsmElementQuestType().isApplicableTo(element);
		return questIsApplicableToElement == null || questIsApplicableToElement;
	}

	private void updateElement(Element newElement)
	{
		if(newElement != null)
		{
			putUpdatedElement(newElement);
		}
		else
		{
			deleteElement(quest.getElementType(), quest.getElementId());
		}
	}

	private Element getElementFromServer(Element.Type elementType, long id)
	{
		switch(elementType)
		{
			case NODE:     return osmDao.getNode(id);
			case WAY:      return osmDao.getWay(id);
			case RELATION: return osmDao.getRelation(id);
		}
		return null;
	}

	private void putUpdatedElement(Element element)
	{
		elementDB.put(element);
		OsmQuestGiver.QuestUpdates questUpdates = questGiver.updateQuests(element);
		createdQuests.addAll(questUpdates.createdQuests);
		removedQuestIds.addAll(questUpdates.removedQuestIds);
	}

	private void deleteElement(Element.Type type, long id)
	{
		elementDB.delete(type, id);
		removedQuestIds.addAll(questGiver.removeQuests(type, id));
	}

	private String getQuestStringForLog()
	{
		return quest.getType().getClass().getSimpleName() + " for " +
			quest.getElementType().name().toLowerCase(Locale.US) + " #" + quest.getElementId();
	}
}
