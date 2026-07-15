package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.parseLocalizedNames
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddBusStopNameForm(
    on: (QuestAction<List<LocalizedName>>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    nameSuggestionsSource: NameSuggestionsSource = koinInject()
) {
    var initialLocalizedNames by rememberSerializable { mutableStateOf<List<LocalizedName>?>(
        parseLocalizedNames(element.tags)
    ) }

    val mapClick = LocalLastMapClick.current
    LaunchedEffect(mapClick) {
        if (mapClick != null) {
            nameSuggestionsSource
                .getNames(mapClick.position, mapClick.clickAreaSizeInMeters, busStopsWithNamesFilter)
                .firstOrNull()
                ?.let { initialLocalizedNames = it }
        }
    }

    LocalizedNameQuestForm(
        on = on,
        countryInfo = countryInfo,
        initialLocalizedNames = initialLocalizedNames,
        hint = {
            Text(stringResource(Res.string.quest_streetName_abbreviation_instruction))
        },
    )
}

// this filter needs to be kept somewhat in sync with the filter in AddBusStopName
private val busStopsWithNamesFilter by lazy { """
    nodes, ways, relations with
    (
      public_transport = platform and bus = yes
      or highway = bus_stop and public_transport != stop_position
      or railway ~ halt|station|tram_stop
    )
    and name
    """.toElementFilterExpression()
}
