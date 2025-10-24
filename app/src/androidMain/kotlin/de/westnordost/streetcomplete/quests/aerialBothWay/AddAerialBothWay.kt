package de.westnordost.streetcomplete.quests.aerialBothWay

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.aerialBothWay.AerialBothWayAnswer.BOTHWAY
import de.westnordost.streetcomplete.quests.aerialBothWay.AerialBothWayAnswer.DOWNWARD
import de.westnordost.streetcomplete.quests.aerialBothWay.AerialBothWayAnswer.UPWARD

class AddAerialBothWay : OsmElementQuestType<AerialBothWayAnswer>, AndroidQuest {

    private val elementFilter by lazy { """
        ways with aerialway and aerialway !~ cable_car|zipline and !oneway
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether aerial ways can be used both ways"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(PEDESTRIAN)

    override val hint = R.string.quest_arrow_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bothway_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> = mapData.ways.filter { elementFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? = elementFilter.matches(element)

    override fun createForm() = AddAerialBothWayForm()

    override fun applyAnswerTo(answer: AerialBothWayAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["oneway"] = when (answer) {
            UPWARD -> "yes"
            DOWNWARD -> "-1"
            BOTHWAY -> "no"
        }
    }
}
