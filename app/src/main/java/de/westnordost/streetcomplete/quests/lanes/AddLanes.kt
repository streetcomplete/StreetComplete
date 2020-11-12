package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddLanes : OsmFilterQuestType<LanesAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ ${ROADS_WITH_LANES.joinToString("|")}
          and (!lanes or lanes !~ [1-9][0-9]*)
    """
    override val commitMessage = "Add road lanes"
    override val wikiLink = "Key:lanes"
    override val icon = R.drawable.ic_quest_street_lanes
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lanes_title

    override fun createForm() = AddLanesForm()

    override fun applyAnswerTo(answer: LanesAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is MarkedLanes -> {
                changes.addOrModify("lanes", answer.count.toString())
                if (answer.count == 1) {
                    changes.deleteIfExists("lanes:forward")
                    changes.deleteIfExists("lanes:backward")
                } else {
                    changes.modifyIfExists("lanes:forward", (answer.count / 2).toString())
                    changes.modifyIfExists("lanes:backward", (answer.count / 2).toString())
                }
                changes.modifyIfExists("lane_markings", "yes")
            }
            is UnmarkedLanes -> {
                changes.addOrModify("lanes", answer.count.toString())
                changes.deleteIfExists("lanes:forward")
                changes.deleteIfExists("lanes:backward")
                // if there is just one lane, the information whether it is marked or not is irrelevant
                // (if there are no more than one lane, there are no markings to separate them)
                if (answer.count == 1) {
                    changes.deleteIfExists("lane_markings")
                } else {
                    changes.addOrModify("lane_markings", "no")
                }
            }
            is MarkedLanesSides -> {
                changes.addOrModify("lanes", (answer.forward + answer.backward).toString())
                changes.addOrModify("lanes:forward", answer.forward.toString())
                changes.addOrModify("lanes:backward", answer.backward.toString())
                changes.modifyIfExists("lane_markings", "yes")
            }
        }
    }

    companion object {
        private val ROADS_WITH_LANES = listOf(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified", "residential"
        )
    }
}

