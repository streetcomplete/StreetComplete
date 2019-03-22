package de.westnordost.streetcomplete.data.osm.upload;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
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
import de.westnordost.streetcomplete.data.osm.download.ElementGeometryCreator;
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class OsmQuestChangeUpload
{
	private final String TAG = "OsmQuestUpload";

	private final MapDataDao osmDao;
	private final AOsmQuestDao questDB;
	private final MergedElementDao elementDB;
	private final ElementGeometryDao elementGeometryDB;
	private final ElementGeometryCreator elementGeometryCreator;
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
		ElementGeometryDao elementGeometryDB, ElementGeometryCreator elementGeometryCreator,
		OsmQuestGiver questGiver)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.elementDB = elementDB;
		this.elementGeometryDB = elementGeometryDB;
		this.questGiver = questGiver;
		this.elementGeometryCreator = elementGeometryCreator;
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
		updateElement(updatedElement);

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
		if (newElement != null && newElement.getVersion() == element.getVersion())
		{
			// a changeset conflict cannot be handled here: throw it along
			throw e;
		}

		// safeguard against stack overflow in case of programming error
		if(alreadyHandlingElementConflict)
		{
			throw new RuntimeException("OSM server continues to report an element " +
				"conflict on uploading the changes for the quest " + getQuestStringForLog() +
				". The local version is " + element.getVersion(), e);
		}
		alreadyHandlingElementConflict = true;

		if (newElement != null)
		{
			if (isGeometrySubstantiallyDifferent(element, newElement))
			{
				// TODO NOTE: when implementing splitting up - what about undo?
				Log.d(TAG, "Dropping quest " + getQuestStringForLog() +
					" and all other quests for the same element because the element's geometry" +
					" changed substantially");
				replaceElement(newElement);
				return false;
			}

			updateElement(newElement);

			// if after updating to the new version of the element, the quest is not applicable to the
			// element anymore, drop it (#720)
			if (shouldCheckForQuestApplicability && !questIsApplicableToElement(newElement))
			{
				Log.d(TAG, "Dropping quest " + getQuestStringForLog() +
					" because the quest is no longer applicable to the element");
				return false;
			}
		}
		else
		{
			deleteElement(quest.getElementType(), quest.getElementId());
		}
		return uploadQuestChange(newElement);

	}

	private boolean questIsApplicableToElement(Element element)
	{
		Boolean questIsApplicableToElement = quest.getOsmElementQuestType().isApplicableTo(element);
		return questIsApplicableToElement == null || questIsApplicableToElement;
	}

	private boolean isGeometrySubstantiallyDifferent(Element element, Element newElement)
	{
		if(element instanceof Node)
			return isNodeGeometrySubstantiallyDifferent((Node) element, (Node) newElement);
		if(element instanceof Way)
			return isWayGeometrySubstantiallyDifferent((Way) element, (Way) newElement);
		if(element instanceof Relation)
			return isRelationGeometrySubstantiallyDifferent((Relation) element, (Relation) newElement);
		return false;
	}

	private boolean isNodeGeometrySubstantiallyDifferent(Node node, Node newNode)
	{
		/* Moving the node a distance beyond what would pass as adjusting the position within a
		   building counts as substantial change. Also, the maximum distance should be not (much)
		   bigger than the usual GPS inaccuracy in the city. */
		double distance = SphericalEarthMath.distance(node.getPosition(), newNode.getPosition());
		return distance > 20;
	}

	private boolean isWayGeometrySubstantiallyDifferent(Way way, Way newWay)
	{
		/* if the first or last node is different, it means that the way has either been extended or
		   shortened at one end, which is counted as being substantial:
		   If for example the surveyor has been asked to determine something for a certain way
		   and this way is now longer, his answer does not apply to the whole way anymore, so that
		   is an unsolvable conflict. */
		List<Long> nodeIds = way.getNodeIds();
		List<Long> newNodeIds = newWay.getNodeIds();

		if(newNodeIds.isEmpty()) return true;

		if(((long) nodeIds.get(0)) != newNodeIds.get(0)) return true;

		int lastIndex = nodeIds.size()-1;
		int lastIndexNew = newNodeIds.size()-1;
		if((long) nodeIds.get(lastIndex) != newNodeIds.get(lastIndexNew)) return true;

		return false;
	}

	private boolean isRelationGeometrySubstantiallyDifferent(Relation relation, Relation newRelation)
	{
		/* a relation is counted as substantially different, if any member changed, even if just
		   the order changed because for some relations, the order has an important meaning */
		return !relation.getMembers().equals(newRelation.getMembers());
	}

	private void updateElement(@NonNull Element newElement)
	{
		elementDB.put(newElement);
		OsmQuestGiver.QuestUpdates questUpdates = questGiver.updateQuests(newElement);
		createdQuests.addAll(questUpdates.createdQuests);
		removedQuestIds.addAll(questUpdates.removedQuestIds);
	}

	private void deleteElement(Element.Type type, long id)
	{
		elementDB.delete(type, id);
		elementGeometryDB.delete(type, id);
		removeQuestsForElement(type, id);
	}

	private void replaceElement(Element element)
	{
		removeQuestsForElement(element.getType(), element.getId());
		updateElement(element);
		elementGeometryDB.put(element.getType(), element.getId(), elementGeometryCreator.create(element));
	}

	private void removeQuestsForElement(Element.Type type, long id)
	{
		List<Long> ids = questDB.getAllIds(type, id);
		questDB.deleteAll(ids);
		removedQuestIds.addAll(ids);
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

	private String getQuestStringForLog()
	{
		return quest.getType().getClass().getSimpleName() + " for " +
			quest.getElementType().name().toLowerCase(Locale.US) + " #" + quest.getElementId();
	}
}
