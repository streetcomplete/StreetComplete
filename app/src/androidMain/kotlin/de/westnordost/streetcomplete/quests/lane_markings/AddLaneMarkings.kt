package de.westnordost.streetcomplete.quests.lane_markings

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.ROADS_ASSUMED_TO_BE_PAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.FILTER_IS_IMPLICIT_MAX_SPEED_BUT_NOT_SLOW_ZONE
import de.westnordost.streetcomplete.osm.surface.PAVED_SURFACES
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddLaneMarkings : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        ways with
          (
            highway ~ ${ROADS_THAT_USUALLY_HAVE_LANE_MARKINGS.joinToString("|")}
            or highway = residential and (
              maxspeed > 33
              or $FILTER_IS_IMPLICIT_MAX_SPEED_BUT_NOT_SLOW_ZONE
            )
          )
          and (surface ~ ${PAVED_SURFACES.joinToString("|")} or highway ~ ${ROADS_ASSUMED_TO_BE_PAVED.joinToString("|")})
          and !lane_markings
          and (!lanes or lanes = 0)
          and (!lanes:backward or !lanes:forward)
          and area != yes
          and placement != transition
          and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val changesetComment = "Specify whether roads have lane markings"
    override val wikiLink = "Key:lane_markings"
    override val icon = R.drawable.quest_street
    override val achievements = listOf(EditTypeAchievement.CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lane_markings_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["lane_markings"] = answer.toYesNo()
    }
}

private val ROADS_THAT_USUALLY_HAVE_LANE_MARKINGS = listOf(
    "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
    "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "busway",
)
