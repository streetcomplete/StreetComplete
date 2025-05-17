package de.westnordost.streetcomplete.quests.pitch_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddPitchLit : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with (leisure = pitch or leisure = track)
        and (access !~ private|no)
        and indoor != yes and (!building or building = no)
        and (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
    """
    override val changesetComment = "Specify whether pitches are lit"
    override val wikiLink = "Key:lit"
    override val icon = R.drawable.ic_quest_pitch_lantern
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lit_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("lit", answer.toYesNo())
    }
}
