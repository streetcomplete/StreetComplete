package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFerryAccessMotorVehicle : OsmElementQuestType<Boolean>, AndroidQuest {

    private val preliminaryFilter by lazy { "ways, relations with route = ferry and !motor_vehicle".toElementFilterExpression()
    }
    override val changesetComment = "Specify ferry access for motor vehicles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_ferry_motor_vehicle_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["motor_vehicle"] = answer.toYesNo()
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // This is the primary method that performs the full check with mapData
        return mapData.ways.filter { element ->
            // First, a quick check with the preliminary filter
            if (!preliminaryFilter.matches(element)) return@filter false
            // Then, the detailed check which includes relation checks
            isApplicableTo(element, mapData)
        }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        // Check if it's a ferry. If yes, check if its part of a ferry route via getApplicableElements.
        val tags = element.tags
        if (tags["route"] == "ferry") {
            return null
        }

        return true
    }

    private fun isApplicableTo(element: Element, mapData: MapDataWithGeometry): Boolean {
        val tags = element.tags

        // Filter out ferries that are part of a ferry route relation
        if (tags["route"] == "ferry") {
            val isPartOfFerryRelation = mapData.relations.any { relation ->
                relation.tags["type"] == "route" &&
                    relation.tags["route"] == "ferry" &&
                    relation.members.any { member ->
                        member.type == ElementType.WAY && member.ref == element.id
                    }
            }
            if (isPartOfFerryRelation) {
                return false
            }
        }

        return true
    }
}
