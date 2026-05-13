package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddBusStopNameForm(
    onAnswer: (BusStopNameAnswer) -> Unit
) {
    val viewModel = koinViewModel<AddBusStopNameFormViewModel>()

    var initialLocalizedNames by rememberSerializable { mutableStateOf<List<LocalizedName>?>(null) }

    var confirmNoName by remember { mutableStateOf(false) }

    // TODO compose-quest-form this is actually not called anywhere yet!
    fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        val names = viewModel.getNamesSuggestionAt(position, clickAreaSizeInMeters)
        if (names != null) {
            initialLocalizedNames = names
        }
        return true
    }

    LocalizedNameQuestForm(
        countryInfo = countryInfo,
        initialLocalizedNames = initialLocalizedNames,
        onClickOk = { onAnswer(BusStopName(it)) },
        onNoNameSign = { confirmNoName = true },
        hint = {
            Text(stringResource(Res.string.quest_streetName_abbreviation_instruction))
        }
    )
    if (confirmNoName) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoName = false },
            onConfirmed = { onAnswer(BusStopNameAnswer.NoName) },
            titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
            confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
        )
    }
}
