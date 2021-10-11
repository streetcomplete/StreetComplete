package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddRailwayCrossingBarrier : OsmElementQuestType<RailwayCrossingBarrier> {

    private val crossingFilter by lazy { """
        nodes with
          railway = level_crossing
          and (!crossing:barrier or crossing:barrier older today -8 years)
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway and access ~ private|no
          or railway ~ tram|abandoned|disused
    """.toElementFilterExpression() }

    override val commitMessage = "Add type of barrier for railway crossing"
    override val wikiLink = "Key:crossing:barrier"
    override val icon = R.drawable.ic_quest_railway

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_crossing_barrier_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    override fun createForm() = AddRailwayCrossingBarrierForm()

    override fun applyAnswerTo(answer: RailwayCrossingBarrier, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("crossing:barrier", answer.osmValue)
    }
}
