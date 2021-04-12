package de.westnordost.streetcomplete.quests.pitch_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddPitchLit : OsmFilterQuestType<PitchLit>() {

    override val elementFilter = """
        ways with (leisure=pitch or leisure=track)
        and (access !~ private|no)
        and indoor != yes and !building
        and (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
    """

    override val commitMessage = "Add whether pitch is lit"
    override val wikiLink = "Key:lit"
    override val icon = R.drawable.ic_quest_lantern

    override fun getTitle(tags: Map<String, String>) =
        if (tags.get("leisure") == "track")
            R.string.quest_pitchLit_title_track
        else
            R.string.quest_pitchLit_title

    override fun createForm() = PitchLitForm()

    override fun applyAnswerTo(answer: PitchLit, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("lit", answer.osmValue)
    }
}
