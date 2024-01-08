package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo

class AddPitchSurface : OsmFilterQuestType<SurfaceAndNote>() {
    private val sportValuesWherePitchSurfaceQuestionIsInteresting = listOf(
        // #2377
        "multi", "soccer", "tennis", "basketball", "equestrian", "athletics", "volleyball",
        "bmx", "american_football", "badminton", "pelota", "horse_racing", "skateboard",
        "disc_golf", "futsal", "cycling", "gymnastics", "bowls", "boules", "netball",
        "handball", "team_handball", "field_hockey", "padel", "horseshoes", "tetherball",
        "gaelic_games", "australian_football", "racquet", "rugby_league", "rugby_union", "rugby",
        "canadian_football", "softball", "sepak_takraw", "cricket", "pickleball", "lacrosse",
        "roller_skating", "baseball", "shuffleboard", "paddle_tennis", "korfball", "petanque",
        "croquet", "four_square", "shot-put",

        // #2468
        "running", "dog_racing", "toboggan",
    )

    override val elementFilter = """
        ways with leisure ~ pitch|track
         and sport ~ "(^|.*;)(${sportValuesWherePitchSurfaceQuestionIsInteresting.joinToString("|")})($|;.*)"
         and (access !~ private|no)
         and indoor != yes and (!building or building = no)
         and (athletics !~ high_jump|pole_vault)
         and (
          !surface
          or surface older today -12 years
          or (
           surface ~ paved|unpaved
           and !surface:note
           and !note:surface
          )
        )
    """

    override val changesetComment = "Specify pitch surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_pitch_surface
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_surface_title

    override fun createForm() = AddPitchSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAndNote, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
