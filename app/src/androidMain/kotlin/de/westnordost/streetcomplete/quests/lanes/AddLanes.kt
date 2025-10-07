package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ROADS_ASSUMED_TO_BE_PAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.FILTER_IS_IMPLICIT_MAX_SPEED_BUT_NOT_SLOW_ZONE
import de.westnordost.streetcomplete.osm.surface.PAVED_SURFACES

class AddLanes : OsmFilterQuestType<LanesAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
          (
            highway ~ ${ROADS_WITH_LANES.joinToString("|")}
            or highway = residential and (
              maxspeed > 33
              or $FILTER_IS_IMPLICIT_MAX_SPEED_BUT_NOT_SLOW_ZONE
            )
          )
          and area != yes
          and (surface ~ ${PAVED_SURFACES.joinToString("|")} or highway ~ ${ROADS_ASSUMED_TO_BE_PAVED.joinToString("|")})
          and (!lanes or lanes = 0)
          and (!lanes:backward or !lanes:forward)
          and lane_markings != no
          and placement != transition
          and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val changesetComment = "Determine roads lane count"
    override val wikiLink = "Key:lanes"
    override val icon = R.drawable.quest_street_lanes
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lanes_title

    override fun createForm() = AddLanesForm()

    override fun applyAnswerTo(answer: LanesAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is UnmarkedLanes -> {
                tags["lane_markings"] = "no"

                // don't touch tags["lanes"] because the user didn't answer anything in this regard
                // but remove these, for unmarked roads, there is no forward/backward
                tags.remove("lanes:forward")
                tags.remove("lanes:backward")

                tags.remove("lanes:both_ways")
                tags.remove("turn:lanes:both_ways")
            }
            is MarkedLanes -> {
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

                // if there is just one lane, the information whether it is marked or not is irrelevant
                // (if there is no more than one lane, there are no markings to separate them)
                if (answer.total == 1) {
                    tags.remove("lane_markings")
                } else {
                    tags["lane_markings"] = "yes"
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

    companion object {
        private val ROADS_WITH_LANES = listOf(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified", "busway",
        )
    }
}
