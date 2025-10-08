package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags

class AddLanes : OsmFilterQuestType<LanesAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
          highway ~ ${ALL_ROADS.joinToString("|")}
          and lane_markings = yes
          and (!lanes or lanes = 0)
          and (!lanes:backward or !lanes:forward)
          and area != yes
          and placement != transition
          and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val changesetComment = "Determine roads lane count"
    override val wikiLink = "Key:lanes"
    override val icon = R.drawable.quest_street_lanes
    override val achievements = listOf(CAR)
    override val hint = R.string.quest_street_side_puzzle_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lanes_title

    override fun createForm() = AddLanesForm()

    override fun applyAnswerTo(answer: LanesAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is LanesAnswer.IsUnmarked -> {
                tags["lane_markings"] = "no"

                // don't touch tags["lanes"] because the user didn't answer anything in this regard
                // but remove these, for unmarked roads, there is no forward/backward
                tags.remove("lanes:forward")
                tags.remove("lanes:backward")
                tags.remove("lanes:both_ways")
                tags.remove("turn:lanes:both_ways")
            }
            is Lanes -> {
                tags["lanes"] = answer.total.toString()

                // only tag forward/backward if both sides are defined (e.g. not on oneways) and
                // if either it has been specified before or forward+backward differ
                if (answer.forward != null && answer.backward != null) {
                    val tagSidesExplicitly = answer.centerLeftTurnLane || answer.forward != answer.backward

                    if (tagSidesExplicitly || tags.containsKey("lanes:forward")) {
                        tags["lanes:forward"] = answer.forward.toString()
                    }
                    if (tagSidesExplicitly || tags.containsKey("lanes:backward")) {
                        tags["lanes:backward"] = answer.backward.toString()
                    }
                }

                if (answer.centerLeftTurnLane) {
                    tags["lanes:both_ways"] = "1"
                    tags["turn:lanes:both_ways"] = "left"
                } else {
                    tags.remove("lanes:both_ways")
                    tags.remove("turn:lanes:both_ways")
                }
            }
        }
    }
}
