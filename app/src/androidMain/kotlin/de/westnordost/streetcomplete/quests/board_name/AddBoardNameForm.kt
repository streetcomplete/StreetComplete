package de.westnordost.streetcomplete.quests.board_name

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBoardNameForm : AbstractOsmQuestForm<BoardNameAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        var confirmNoName by remember { mutableStateOf(false) }

        LocalizedNameQuestForm(
            prefs = prefs,
            countryInfo = countryInfo,
            initialLocalizedNames = null,
            onClickOk = { applyAnswer(BoardName(it)) },
            onNoNameSign = { confirmNoName = true },
        )

        if (confirmNoName) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoName = false },
                onConfirmed = { applyAnswer(BoardNameAnswer.NoName) },
                titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
                confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
            )
        }
    }
}
