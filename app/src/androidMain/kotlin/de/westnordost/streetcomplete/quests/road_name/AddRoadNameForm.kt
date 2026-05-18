package de.westnordost.streetcomplete.quests.road_name

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddRoadNameForm(
    onAnswer: (RoadNameAnswer) -> Unit,
    countryInfo: CountryInfo,
    nameSuggestionsSource: NameSuggestionsSource = koinInject()
) {
    var initialLocalizedNames by rememberSerializable { mutableStateOf<List<LocalizedName>?>(null) }

    var confirmNoStreetName by remember { mutableStateOf(false) }

    // TODO compose-quest-form this is actually not called anywhere yet!
    fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.let { initialLocalizedNames = it }

        return true
    }

    LocalizedNameQuestForm(
        countryInfo = countryInfo,
        initialLocalizedNames = initialLocalizedNames,
        onClickOk = { onAnswer(RoadName(it)) },
        onNoNameSign = { confirmNoStreetName = true },
        hint = {
            Text(stringResource(Res.string.quest_streetName_abbreviation_instruction))
        }
    )

    if (confirmNoStreetName) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoStreetName = false },
            onConfirmed = { onAnswer(RoadNameAnswer.NoName) },
            titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
            text = { Text(stringResource(Res.string.quest_streetName_answer_noName_confirmation_description)) },
            confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
        )
    }
}

private val roadsWithNamesFilter by lazy {
    "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
        .toElementFilterExpression()
}
