package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED

class AddRoadSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway ~ ${listOf(
            "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified", "residential", "living_street", "pedestrian", "track",
            ).joinToString("|")
          }
          or highway = service and service !~ driveway|slipway
        )
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -6 years
          or surface older today -12 years
          or (
            surface ~ paved|unpaved|cobblestone
            and !surface:note
            and !note:surface
          )
          or tracktype = grade1 and surface ~ sand|gravel|fine_gravel|compacted|grass|earth|dirt|mud|pebbles|unpaved
          or tracktype = grade2 and surface ~ sand|grass|earth|dirt|mud
          or tracktype ~ grade3|grade4|grade5 and surface ~ asphalt|concrete|paving_stones|paved
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add road surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") R.string.quest_streetSurface_square_title
        else                       R.string.quest_streetSurface_title

    override fun createForm() = AddRoadSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "surface")
    }
}
