package de.westnordost.streetcomplete.data.osm;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;

/** Manages creating new quests and removing quests that are no longer applicable for an OSM
 *  element locally */
public class OsmQuestGiver
{
	private static final String TAG = "OsmQuestGiver";

	private final OsmNoteQuestDao osmNoteQuestDb;
	private final OsmQuestDao questDB;
	private final ElementGeometryDao elementGeometryDB;
	private final Provider<List<QuestType>> questTypesProvider;

	@Inject public OsmQuestGiver(
			OsmNoteQuestDao osmNoteQuestDb, OsmQuestDao questDB,
			ElementGeometryDao elementGeometryDB, Provider<List<QuestType>> questTypesProvider)
	{
		this.osmNoteQuestDb = osmNoteQuestDb;
		this.questDB = questDB;
		this.elementGeometryDB = elementGeometryDB;
		this.questTypesProvider = questTypesProvider;
	}

	public static class QuestUpdates
	{
		public List<OsmQuest> createdQuests = new ArrayList<>();
		public List<Long> removedQuestIds = new ArrayList<>();
	}

	public QuestUpdates updateQuests(Element element)
	{
		ElementGeometry geometry = elementGeometryDB.get(element.getType(), element.getId());
		boolean hasNote = hasNoteAt(geometry.center);

		QuestUpdates result = new QuestUpdates();

		Map<QuestType, OsmQuest> currentQuests = getCurrentQuests(element);
		List<String> createdQuestsLog = new ArrayList<>();
		List<String> removedQuestsLog = new ArrayList<>();

		for(QuestType questType : questTypesProvider.get())
		{
			if(!(questType instanceof OsmElementQuestType)) continue;
			OsmElementQuestType osmQuestType = (OsmElementQuestType)questType;

			Boolean appliesToElement = osmQuestType.isApplicableTo(element);
			if(appliesToElement == null) continue;

			boolean hasQuest = currentQuests.containsKey(osmQuestType);
			if(appliesToElement && !hasQuest && !hasNote)
			{
				OsmQuest quest = new OsmQuest(osmQuestType, element.getType(), element.getId(), geometry);
				result.createdQuests.add(quest);
				createdQuestsLog.add(osmQuestType.getClass().getSimpleName());
			}
			if(!appliesToElement && hasQuest)
			{
				OsmQuest quest = currentQuests.get(osmQuestType);
				// only remove "fresh" unanswered quests because answered/closed quests by definition
				// do not apply to the element anymore. E.g. after adding the name to the street,
				// there shan't be any AddRoadName quest for that street anymore
				if(quest.getStatus() == QuestStatus.NEW)
				{
					result.removedQuestIds.add(quest.getId());
					removedQuestsLog.add(osmQuestType.getClass().getSimpleName());
				}
			}
		}

		if(!result.createdQuests.isEmpty())
		{
			// Before new quests are unlocked, all reverted quests need to be removed for
			// this element so that they can be created anew as the case may be
			questDB.deleteAllReverted(element.getType(), element.getId());

			questDB.addAll(result.createdQuests);

			Log.d(TAG, "Created new quests for " + element.getType().name() + "#" + element.getId() + ": " +
				TextUtils.join(", ", createdQuestsLog)
			);
		}
		if(!result.removedQuestIds.isEmpty())
		{
			questDB.deleteAll(result.removedQuestIds);

			Log.d(TAG, "Removed quests no longer applicable for " + element.getType().name() + "#" + element.getId() + ": " +
				TextUtils.join(", ", removedQuestsLog)
			);
		}

		return result;
	}

	private boolean hasNoteAt(LatLon pos)
	{
		BoundingBox bbox = new BoundingBox(pos, pos);
		return !osmNoteQuestDb.getAllPositions(bbox).isEmpty();
	}

	private Map<QuestType, OsmQuest> getCurrentQuests(Element element)
	{
		List<OsmQuest> quests = questDB.getAll(null, null, null, element.getType(), element.getId());
		Map<QuestType, OsmQuest> result = new HashMap<>(quests.size());
		for (OsmQuest quest : quests)
		{
			if(quest.getStatus() == QuestStatus.REVERT) continue;
			result.put(quest.getType(), quest);
		}
		return result;
	}
}
