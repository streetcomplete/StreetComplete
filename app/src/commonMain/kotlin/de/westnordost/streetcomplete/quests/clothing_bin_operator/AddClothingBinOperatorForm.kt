package de.westnordost.streetcomplete.quests.clothing_bin_operator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.AltAnswer
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddClothingBinOperatorForm(
    onAnswer: (QuestAnswer<ClothingBinOperatorAnswer>) -> Unit,
    countryInfo: CountryInfo,
) {
    var confirmNoSign by remember { mutableStateOf(false) }

    NameWithSuggestionsQuestForm(
        suggestions = countryInfo.clothesContainerOperators,
        onAnswer = {
            onAnswer(when (it) {
                is Answer<String> -> Answer(ClothingBinOperator(it.value))
                is AltAnswer -> it
            })
        },
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_answer_noSign)) { confirmNoSign = true }
        )
    )

    if (confirmNoSign) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { onAnswer(Answer(ClothingBinOperatorAnswer.NoneSigned)) }
        )
    }
}
