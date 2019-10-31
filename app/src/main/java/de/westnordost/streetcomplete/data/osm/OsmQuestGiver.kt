package de.westnordost.streetcomplete.data.osm

import android.util.Log


import javax.inject.Inject

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.util.SphericalEarthMath
import javax.inject.Named
import javax.inject.Provider

/** Manages creating new quests and removing quests that are no longer applicable for an OSM
 * element locally  */
class OsmQuestGiver @Inject constructor(
    private val osmNoteQuestDb: OsmNoteQuestDao,
    private val questDB: OsmQuestDao,
    private val elementGeometryDB: ElementGeometryDao,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider
) {

	private val TAG = "OsmQuestGiver"

    class QuestUpdates {
        val createdQuests: MutableList<OsmQuest> = ArrayList()
        val removedQuestIds: MutableList<Long> = ArrayList()
    }

    fun updateQuests(element: Element): QuestUpdates {
        val result = QuestUpdates()

        val geometry = elementGeometryDB.get(element.type, element.id) ?: return result

        val hasNote = hasNoteAt(geometry.center)

        val currentQuests = getCurrentQuests(element)
        val createdQuestsLog = ArrayList<String>()
        val removedQuestsLog = ArrayList<String>()

        for (questType in questTypesProvider.get()) {
            if (questType !is OsmElementQuestType<*>) continue

            val appliesToElement = questType.isApplicableTo(element) ?: continue

            val hasQuest = currentQuests.containsKey(questType)
            if (appliesToElement && !hasQuest && !hasNote) {
                val quest = OsmQuest(questType, element.type, element.id, geometry)
                result.createdQuests.add(quest)
                createdQuestsLog.add(questType.javaClass.simpleName)
            }
            if (!appliesToElement && hasQuest) {
                val quest = currentQuests.getValue(questType)
                // only remove "fresh" unanswered quests because answered/closed quests by definition
                // do not apply to the element anymore. E.g. after adding the name to the street,
                // there shan't be any AddRoadName quest for that street anymore
                if (quest.status == QuestStatus.NEW) {
                    result.removedQuestIds.add(quest.id!!)
                    removedQuestsLog.add(questType.javaClass.simpleName)
                }
            }
        }

        if (result.createdQuests.isNotEmpty()) {
            // Before new quests are unlocked, all reverted quests need to be removed for
            // this element so that they can be created anew as the case may be
            questDB.deleteAllReverted(element.type, element.id)

            questDB.addAll(result.createdQuests)
            Log.d(TAG, "Created new quests for ${element.type.name}#${element.id}: ${createdQuestsLog.joinToString()}")
        }
        if (result.removedQuestIds.isNotEmpty()) {
            questDB.deleteAll(result.removedQuestIds)
            Log.d(TAG, "Removed quests no longer applicable for ${element.type.name}#${element.id}: ${removedQuestsLog.joinToString()}")
        }

        return result
    }

    fun deleteQuests(elementType: Element.Type, elementId: Long): List<Long> {
        val ids = questDB.getAllIds(elementType, elementId)
        questDB.deleteAll(ids)

        Log.d(TAG, "Removed all quests for deleted element " + elementType.name + "#" + elementId)
	    return ids
    }

    private fun hasNoteAt(pos: LatLon): Boolean {
        // note about one meter around the center of an element still count as at this point as to
        // deal with imprecision of the center calculation of geometry (see #1089)
        val bbox = SphericalEarthMath.enclosingBoundingBox(pos, 1.0)
        return osmNoteQuestDb.getAllPositions(bbox).isNotEmpty()
    }

    private fun getCurrentQuests(element: Element): Map<QuestType<*>, OsmQuest> {
        val quests = questDB.getAll(null, null, null, element.type, element.id)
        val result = HashMap<QuestType<*>, OsmQuest>(quests.size)
        for (quest in quests) {
            if (quest.status == QuestStatus.REVERT) continue
            result[quest.type] = quest
        }
        return result
    }
}
