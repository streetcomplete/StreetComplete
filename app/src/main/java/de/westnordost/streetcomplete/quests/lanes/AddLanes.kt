package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ROADS_ASSUMED_TO_BE_PAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isImplicitMaxSpeedButNotSlowZone
import de.westnordost.streetcomplete.osm.surface.PAVED_SURFACES

class AddLanes : OsmFilterQuestType<LanesAnswer>() {

    override val elementFilter = """
        ways with
          (
            highway ~ ${ROADS_WITH_LANES.joinToString("|")}
            or highway = residential and (
              maxspeed > 33
              or $isImplicitMaxSpeedButNotSlowZone
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
    override val icon = R.drawable.ic_quest_street_lanes
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lanes_title

    override fun createForm() = AddLanesForm()

    override fun applyAnswerTo(answer: LanesAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val laneCount = answer.total

        laneCount?.let { tags["lanes"] = it.toString() }

        val isMarked = answer !is UnmarkedLanes
        // if there is just one lane, the information whether it is marked or not is irrelevant
        // (if there are no more than one lane, there are no markings to separate them)
        when {
            laneCount == 1 -> {
                tags.remove("lane_markings")
            }
            isMarked -> {
                if (tags.containsKey("lane_markings")) {
                    tags["lane_markings"] = "yes"
                }
            }
            else -> {
                tags["lane_markings"] = "no"
            }
        }

        val hasCenterLeftTurnLane = answer is MarkedLanesSides && answer.centerLeftTurnLane
        if (hasCenterLeftTurnLane) {
            tags["lanes:both_ways"] = "1"
            tags["turn:lanes:both_ways"] = "left"
        } else {
            tags.remove("lanes:both_ways")
            tags.remove("turn:lanes:both_ways")
        }

        when (answer) {
            is MarkedLanes -> {
                if (answer.count == 1) {
                    tags.remove("lanes:forward")
                    tags.remove("lanes:backward")
                } else {
                    if (tags.containsKey("lanes:forward")) {
                        tags["lanes:forward"] = (answer.count / 2).toString()
                    }
                    if (tags.containsKey("lanes:backward")) {
                        tags["lanes:backward"] = (answer.count / 2).toString()
                    }
                }
            }
            is UnmarkedLanes -> {
                tags.remove("lanes:forward")
                tags.remove("lanes:backward")
            }
            is MarkedLanesSides -> {
                tags["lanes:forward"] = answer.forward.toString()
                tags["lanes:backward"] = answer.backward.toString()
            }
        }
    }

    companion object {
        private val ROADS_WITH_LANES = listOf(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified"
        )
    }
}
