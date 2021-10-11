package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN

class AddCrossingType : OsmElementQuestType<CrossingType> {

    /*
       Always ask for deprecated/meaningless values (island, unknown, yes)

       Only ask again for crossing types that are known to this quest so to be conservative with
       existing data
     */
    private val crossingFilter by lazy { """
        nodes with highway = crossing
          and foot != no
          and (
            !crossing
            or crossing ~ island|unknown|yes
            or (
              crossing ~ traffic_signals|uncontrolled|zebra|marked|unmarked
              and crossing older today -8 years
            )
          )
    """.toElementFilterExpression()}

    private val excludedWaysFilter by lazy { """
        ways with
          highway and access ~ private|no
    """.toElementFilterExpression()}

    override val commitMessage = "Add crossing type"
    override val wikiLink = "Key:crossing"
    override val icon = R.drawable.ic_quest_pedestrian_crossing

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_type_title

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

    override fun createForm() = AddCrossingTypeForm()

    override fun applyAnswerTo(answer: CrossingType, changes: StringMapChangesBuilder) {
        val previous = changes.getPreviousValue("crossing")
        if(previous == "island") {
            changes.modify("crossing", answer.osmValue)
            changes.addOrModify("crossing:island", "yes")
        } else {
            if (answer == CrossingType.MARKED && previous in listOf("zebra", "marked", "uncontrolled")) {
                changes.updateCheckDateForKey("crossing")
            } else {
                changes.updateWithCheckDate("crossing", answer.osmValue)
            }
        }
    }
}
