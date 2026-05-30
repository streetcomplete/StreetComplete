package de.westnordost.streetcomplete.quests.pitch_lit

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddPitchLit : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with (
            leisure ~ pitch|track|fitness_station
            or piste:type and !highway
        )
        and (access !~ private|no)
        and indoor != yes and (!building or building = no)
        and (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
        and !piste:lit
    """
    override val changesetComment = "Specify whether pitches are lit"
    override val wikiLink = "Key:lit"
    override val icon = Res.drawable.quest_pitch_lantern
    override val title = Res.string.quest_lit_title
    override val achievements = listOf(OUTDOORS)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        // Pistes more commonly use the piste:lit tag, while other leisure activities use lit.
        if (tags.contains("piste:type")) {
            tags["piste:lit"] = answer.toYesNo()
        } else {
            tags.updateWithCheckDate("lit", answer.toYesNo())
        }
    }
}
