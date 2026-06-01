package de.westnordost.streetcomplete.quests.firewood

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFirewood : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
            (
                leisure = firepit
                or (amenity = bbq and fuel = wood)
                or (tourism = wilderness_hut and fireplace = yes)
            )
            and access !~ private|no
            and !wood_provided
    """

    // for now only enabled in the nordics / high-trust societies
    override val enabledInCountries = NoCountriesExcept(
        "SE",
        "DK",
        "FI",
        "NO",
        "CH"
    )
    override val changesetComment = "Specified if firewood is provided"
    override val wikiLink = "Tag:leisure=firepit"
    override val icon = Res.drawable.quest_firewood
    override val title = Res.string.quest_firewood_provided_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with leisure = firepit or amenity = bbq or tourism = wilderness_hut")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["wood_provided"] = answer.toYesNo()
    }
}
