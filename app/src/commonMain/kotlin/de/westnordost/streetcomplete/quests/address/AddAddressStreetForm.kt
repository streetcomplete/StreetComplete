package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddAddressStreetForm(
    on: (QuestAction<StreetOrPlaceName>) -> Unit,
    nameSuggestionsSource: NameSuggestionsSource = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    /* if user specified last time that a housenumber does not belong to a named street in this
       session, already pre-select this. It is likely that the next housenumber he'll answer this
       quest for also doesn't belong to a named street */
    var streetOrPlaceName by rememberSerializable { mutableStateOf<StreetOrPlaceName>(
        if (lastWasPlaceName) PlaceName("") else StreetName("")
    ) }
    /* only show select to switch between place and street name when the user already selected
       place name (i.e. "does not belong to a named street") */
    var showSelect by rememberSaveable { mutableStateOf(lastWasPlaceName) }

    val mapClick = LocalLastMapClick.current
    LaunchedEffect(mapClick) {
        if (mapClick != null) {
            // only allow selection of street when that field is actually displayed
            if (streetOrPlaceName !is StreetName) return@LaunchedEffect

            nameSuggestionsSource
                .getNames(mapClick.position, mapClick.clickAreaSizeInMeters, roadsWithNamesFilter)
                .firstOrNull()
                ?.find { it.languageTag.isEmpty() }
                ?.name
                ?.let { streetOrPlaceName = StreetName(it) }
        }
    }

    QuestForm(
        on = on,
        isComplete =
            streetOrPlaceName.name.isNotEmpty() &&
            streetOrPlaceName.name.length <= MAX_OSM_TAG_VALUE_LENGTH,
        onClickOk = {
            lastWasPlaceName = streetOrPlaceName is PlaceName
            on(Answer(streetOrPlaceName))
        },
        subtitle = nameAndLocationLabel(LocalElement.current!!, featureDictionary, showHouseNumber = true),
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_address_street_no_named_streets)) {
                streetOrPlaceName = PlaceName("")
                showSelect = true
            }
        ) }
    ) {
        StreetOrPlaceNameForm(
            value = streetOrPlaceName,
            onValueChange = { streetOrPlaceName = it },
            modifier = Modifier.fillMaxWidth(),
            showSelect = showSelect
        )
    }
}

/** Whether user answered that the housenumber does not belong to a named street the last time
 *  he answered it in this session */
private var lastWasPlaceName = false

private val roadsWithNamesFilter by lazy {
    "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
        .toElementFilterExpression()
}
