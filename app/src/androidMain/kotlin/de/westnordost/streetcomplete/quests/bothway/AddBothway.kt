package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.estimateUsableRoadwayWidth
import de.westnordost.streetcomplete.quests.oneway.BothwayAnswer.BACKWARD
import de.westnordost.streetcomplete.quests.oneway.BothwayAnswer.FORWARD
import de.westnordost.streetcomplete.quests.oneway.BothwayAnswer.NO_ONEWAY

class AddBothway : OsmElementQuestType<BothwayAnswer>, AndroidQuest {

    private val elementFilter by lazy { """
        ways with aerialway and aerialway !~ cable_car|zipline and !oneway
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether aerial ways can be used both ways"
    override val wikiLink = "Key:bothway"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)

    override val hint = R.string.quest_arrow_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bothway_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.ways.filter { elementFilter.matches(it) }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        return elementFilter.matches(element)
    }

    override fun createForm() = AddBothwayForm()

    override fun applyAnswerTo(answer: BothwayAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["oneway"] = when (answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        }
    }
}
