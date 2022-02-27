package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddRoadSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway ~ ${ROADS_TO_ASK_SURFACE_FOR.joinToString("|")}
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
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add road surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"
        return when {
            hasName && isSquare ->  R.string.quest_streetSurface_square_name_title
            hasName ->              R.string.quest_streetSurface_name_title
            isSquare ->             R.string.quest_streetSurface_square_title
            else ->                 R.string.quest_streetSurface_title
        }
    }

    override fun createForm() = AddRoadSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "surface")
    }
}

private val ROADS_TO_ASK_SURFACE_FOR = arrayOf(
    // "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track",
    // "service", // this is too much, and the information value is very low
)
