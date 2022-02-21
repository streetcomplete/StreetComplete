package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddBarrierOnRoad : OsmElementQuestType<BarrierType> {

    private val barrierFilter by lazy { """
        ways with
          barrier ~ wall|fence|hedge|guard_rail|retaining_wall|city_wall
          and area != yes
    """.toElementFilterExpression() }

    private val pathsFilter by lazy { """
        ways with
          (highway ~ ${ALL_ROADS.joinToString("|")} and area != yes)
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    override val changesetComment = "Add how road and barrier intersect"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.ic_quest_barrier_on_road
    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_road_intersection

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        detectWayBarrierIntersection(mapData, barrierFilter, pathsFilter)

    override fun isApplicableTo(element: Element): Boolean? =
        if (element !is Node || element.tags.isNotEmpty()) false else null

    override fun createForm() = AddBarrierTypeForm()

    override fun applyAnswerTo(answer: BarrierType, tags: Tags, timestampEdited: Long) =
        answer.applyTo(tags)
}
