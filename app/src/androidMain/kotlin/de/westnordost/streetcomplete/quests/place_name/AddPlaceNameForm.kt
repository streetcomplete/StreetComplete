package de.westnordost.streetcomplete.quests.place_name

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPlaceNameForm(
    onAnswer: (PlaceNameAnswer) -> Unit,
    countryInfo: CountryInfo
) {
    var confirmNoName by remember { mutableStateOf(false) }

    LocalizedNameQuestForm(
        countryInfo = countryInfo,
        initialLocalizedNames = null,
        onClickOk = { onAnswer(PlaceName(it)) },
        onNoNameSign = { confirmNoName = true },
    )

    if (confirmNoName) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoName = false },
            onConfirmed = { onAnswer(PlaceNameAnswer.NoNameSign) },
            titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
            confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
        )
    }
}
