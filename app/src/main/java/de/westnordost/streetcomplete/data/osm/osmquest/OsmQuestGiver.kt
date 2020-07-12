package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.isInAny


import javax.inject.Inject

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import java.util.concurrent.FutureTask

/** Manages creating new quests and removing quests that are no longer applicable for an OSM
 * element locally  */
class OsmQuestGiver @Inject constructor(
    private val notePositionsSource: NotePositionsSource,
    private val osmQuestController: OsmQuestController,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>
) {

    /** (Re)create quests for the given element, without making any eligibility checks */
    fun recreateQuests(element: Element, geometry: ElementGeometry, recreateQuestTypes: List<OsmElementQuestType<*>>) {
        val createdQuests: MutableList<OsmQuest> = ArrayList()
        val createdQuestsLog = ArrayList<String>()
        for (questType in recreateQuestTypes) {
            val quest = OsmQuest(questType, element.type, element.id, geometry)
            createdQuests.add(quest)
            createdQuestsLog.add(questType.javaClass.simpleName)
        }
        val updates = osmQuestController.updateForElement(createdQuests, emptyList(), element.type, element.id)
        Log.d(TAG, "Recreated ${updates.added} quests for ${element.type.name}#${element.id}: ${createdQuestsLog.joinToString()}")
    }

    /** Update quests for the given element (assuming the element has just been updated) */
    fun updateQuests(element: Element, geometry: ElementGeometry) {
        val createdQuests: MutableList<OsmQuest> = ArrayList()
        val removedQuestIds: MutableList<Long> = ArrayList()

        val hasNote = hasNoteAt(geometry.center)

        val currentQuestsByType = osmQuestController.getAllForElement(element.type, element.id).associateBy { it.type }
        val createdQuestsLog = ArrayList<String>()
        val removedQuestsLog = ArrayList<String>()

        for (questType in questTypesProvider.get()) {
            if (questType !is OsmElementQuestType<*>) continue

            val appliesToElement = questType.isApplicableTo(element) ?: continue
            val countries = questType.enabledInCountries
            val isEnabledForCountry = countryBoundariesFuture.get().isInAny(geometry.center, countries)

            val hasQuest = currentQuestsByType.containsKey(questType)
            if (appliesToElement && !hasQuest && !hasNote && isEnabledForCountry) {
                val quest = OsmQuest(questType, element.type, element.id, geometry)
                createdQuests.add(quest)
                createdQuestsLog.add(questType.javaClass.simpleName)
            }
            if (!appliesToElement && hasQuest) {
                val quest = currentQuestsByType.getValue(questType)
                // only remove "fresh" unanswered quests because answered/closed quests by definition
                // do not apply to the element anymore. E.g. after adding the name to the street,
                // there shan't be any AddRoadName quest for that street anymore
                if (quest.status == QuestStatus.NEW) {
                    removedQuestIds.add(quest.id!!)
                    removedQuestsLog.add(questType.javaClass.simpleName)
                }
            }
        }
        val updates = osmQuestController.updateForElement(createdQuests, removedQuestIds, element.type, element.id)

        if (updates.added > 0) {
            Log.d(TAG, "Created ${updates.added} new quests for ${element.type.name}#${element.id}: ${createdQuestsLog.joinToString()}")
        }
        if (updates.deleted > 0) {
            Log.d(TAG, "Removed ${updates.deleted} quests no longer applicable for ${element.type.name}#${element.id}: ${removedQuestsLog.joinToString()}")
        }
    }

    /** Delete quests for the given element (assuming the element has just been deleted or has
     * invalid geometry) */
    fun deleteQuests(elementType: Element.Type, elementId: Long) {
        osmQuestController.deleteAllForElement(elementType, elementId)
    }

    private fun hasNoteAt(pos: LatLon): Boolean {
        // note about one meter around the center of an element still count as at this point as to
        // deal with imprecision of the center calculation of geometry (see #1089)
        val bbox = pos.enclosingBoundingBox(1.0)
        return notePositionsSource.getAllPositions(bbox).isNotEmpty()
    }

    companion object {
        private const val TAG = "OsmQuestGiver"
    }
}
