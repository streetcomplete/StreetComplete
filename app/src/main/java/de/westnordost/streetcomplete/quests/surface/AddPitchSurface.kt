package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder


class AddPitchSurface : OsmFilterQuestType<SurfaceAnswer>() {
    private val sportValuesWherePitchSurfaceQuestionIsInteresting = listOf(
        "multi", "soccer", "tennis", "basketball", "equestrian", "athletics", "volleyball",
        "bmx", "american_football", "badminton", "pelota", "horse_racing", "skateboard",
        "disc_golf", "futsal", "cycling", "gymnastics", "bowls", "boules", "netball",
        "handball", "team_handball", "field_hockey", "padel", "horseshoes", "tetherball",
        "gaelic_games", "australian_football", "racquet", "rugby_league", "rugby_union", "rugby",
        "canadian_football", "softball", "sepak_takraw", "cricket", "pickleball", "lacrosse",
        "roller_skating", "baseball", "shuffleboard", "paddle_tennis", "korfball", "petanque",
        "croquet", "four_square", "shot-put",
    )

    override val elementFilter = """
        ways with leisure=pitch
        and sport ~ "(^|.*;)(${sportValuesWherePitchSurfaceQuestionIsInteresting.joinToString("|")})(${'$'}|;.*)"
        and (access !~ private|no)
        and indoor != yes and !building
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

    override val commitMessage = "Add pitch surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_pitch_surface

    override fun getTitle(tags: Map<String, String>) = R.string.quest_pitchSurface_title



    override fun createForm() = AddPitchSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SpecificSurfaceAnswer -> {
                changes.updateWithCheckDate("surface", answer.value)
                changes.deleteIfExists("surface:note")
            }
            is GenericSurfaceAnswer -> {
                changes.updateWithCheckDate("surface", answer.value)
                changes.addOrModify("surface:note", answer.note)
            }
        }
        changes.deleteIfExists("source:surface")
    }
}
