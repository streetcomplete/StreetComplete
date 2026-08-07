package de.westnordost.streetcomplete.quests.fire_hydrant_ref

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddFireHydrantRef : OsmFilterQuestType<FireHydrantRefAnswer>() {

    override val elementFilter = """
        nodes with
        emergency = fire_hydrant
        and !name and !ref and noref != yes and ref:signed != no and !~"ref:.*"
    """
    override val changesetComment = "Determine fire hydrant refs"
    override val wikiLink = "Key:ref"
    override val icon = Res.drawable.quest_fire_hydrant_ref
    override val title = Res.string.quest_genericRef_title
    override val achievements = listOf(EditTypeAchievement.LIFESAVER)
    override val enabledInCountries = NoCountriesExcept(
        "CH", "FR", "LI"
    )

    @Composable
    override fun Form(on: (QuestAction<FireHydrantRefAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddFireHydrantRefForm(on)
    }

    override fun applyAnswerTo(answer: FireHydrantRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is FireHydrantRefAnswer.NoSign -> tags["ref:signed"] = "no"
            is FireHydrantRef ->              tags["ref"] = answer.ref
        }
    }
}
