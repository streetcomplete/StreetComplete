package de.westnordost.streetcomplete.quests.toilets_fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddToiletsFee : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          amenity = toilets
          and access !~ private|customers
          and !fee
          and (!seasonal or seasonal = no)
    """
    override val changesetComment = "Specify toilet fees"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.quest_toilet_fee
    override val title = Res.string.quest_toiletsFee_title
    override val enabledInCountries = AllCountriesExcept("US", "CA")
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
}
