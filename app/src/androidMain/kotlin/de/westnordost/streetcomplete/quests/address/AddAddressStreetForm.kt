package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameForm
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_address_street_no_named_streets
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddAddressStreetForm : AbstractOsmQuestForm<StreetOrPlaceName>() {

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    private var streetOrPlaceName = mutableStateOf<StreetOrPlaceName>(
        if (lastWasPlaceName) PlaceName("") else StreetName("")
    )

    @Composable
    override fun Content() {
        var showSelect by rememberSaveable { mutableStateOf(lastWasPlaceName) }

        QuestForm(
            answers = Confirm(isComplete = streetOrPlaceName.value.name.isNotEmpty()) {
                lastWasPlaceName = streetOrPlaceName.value is PlaceName
                applyAnswer(streetOrPlaceName.value)
            },
            subtitle = nameAndLocationLabel(element, featureDictionary, showHouseNumber = true),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_address_street_no_named_streets)) {
                    streetOrPlaceName.value = PlaceName("")
                    showSelect = true
                }
            ),
        ) {
            StreetOrPlaceNameForm(
                value = streetOrPlaceName.value,
                onValueChange = { streetOrPlaceName.value = it },
                modifier = Modifier.fillMaxWidth(),
                showSelect = showSelect
            )
        }
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (streetOrPlaceName.value !is StreetName) return false

        val name = nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.find { it.languageTag.isEmpty() }
            ?.name
            ?: return false

        streetOrPlaceName.value = StreetName(name)
        return true
    }

    companion object {
        private var lastWasPlaceName = false
    }
}
