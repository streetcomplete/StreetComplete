package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.resources.*

class AddBusStopName : OsmFilterQuestType<List<LocalizedName>>() {

    // this filter needs to be kept somewhat in sync with the filter in AddBusStopNameForm https://github.com/streetcomplete/StreetComplete/issues/6390#issuecomment-3057235984
    override val elementFilter = """
        nodes, ways, relations with
        (
          public_transport = platform and bus = yes
          or highway = bus_stop and public_transport != stop_position
          or railway ~ halt|station|tram_stop
        )
        and access !~ no|private
        and !name and noname != yes and name:signed != no
    """

    override val enabledInCountries = AllCountriesExcept(
        "US", // https://github.com/streetcomplete/StreetComplete/issues/2126
        "CA", // https://github.com/streetcomplete/StreetComplete/issues/2126
        "SE" // https://github.com/streetcomplete/StreetComplete/issues/6390#issuecomment-3057235984
    )
    override val changesetComment = "Determine public transport stop names"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = Res.drawable.quest_bus_stop_name
    override val title = Res.string.quest_busStopName_title2
    override val achievements = listOf(PEDESTRIAN)
    override val hint = Res.string.quest_stopName_hint

    @Composable
    override fun Form(on: (QuestAction<List<LocalizedName>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBusStopNameForm(on, countryInfo)
    }

    override fun applyAnswerTo(answer: List<LocalizedName>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.isEmpty()) {
            tags["name:signed"] = "no"
        } else {
            answer.applyTo(tags)
        }
    }
}
