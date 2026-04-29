package de.westnordost.streetcomplete.quests.clothing_bin_operator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm
import org.jetbrains.compose.resources.stringResource

class AddClothingBinOperatorForm : AbstractOsmQuestForm<ClothingBinOperatorAnswer>() {

    @Composable
    override fun Content() {
        var confirmNoSign by remember { mutableStateOf(false) }

        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.clothesContainerOperators,
            onClickOk = { applyAnswer(ClothingBinOperator(it)) },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_generic_answer_noSign)) { confirmNoSign = true }
            )
        )

        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { applyAnswer(ClothingBinOperatorAnswer.NoneSigned) }
            )
        }
    }
}
