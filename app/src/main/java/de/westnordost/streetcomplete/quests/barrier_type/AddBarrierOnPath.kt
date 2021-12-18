package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_PATHS
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddBarrierOnPath: OsmElementQuestType<BarrierType> {

    private val barrierFilter by lazy {
        """
        ways with
          barrier ~ wall|fence|hedge|guard_rail|retaining_wall|city_wall
          and area != yes
    """.toElementFilterExpression()
    }

    private val pathsFilter by lazy {
        """
        ways with
          (highway ~ ${ALL_PATHS.joinToString("|")} and area != yes)
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression()
    }

    override val commitMessage = "Add what is on intersection of path and barrier"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.ic_quest_barrier_on_path
    override val questTypeAchievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_path_intersection

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return detectWayBarrierIntersection(mapData, barrierFilter, pathsFilter)
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (element !is Node || element.tags.isNotEmpty()) false else null

    override fun createForm() = AddBarrierTypeForm()

    override fun applyAnswerTo(answer: BarrierType, changes: StringMapChangesBuilder) = answer.applyTo(changes)
}
