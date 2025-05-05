package de.westnordost.streetcomplete.quests.list_quests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.CREDIT_CARDS_ONLY
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.DEBIT_AND_CREDIT
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.DEBIT_CARDS_ONLY
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.NEITHER_DEBIT_NOR_CREDIT

@Composable
fun <T> ListQuestForm(options: List<TextItem<T>>, onSelectionChange: (TextItem<T>) -> Unit, currentOption: TextItem<T>?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        options.forEach {
            Row(modifier = Modifier
                .padding(vertical = 8.dp)
                .selectable(
                    selected = (it == currentOption),
                    onClick = { onSelectionChange(it) },
                    role = Role.RadioButton
                )) {
                RadioButton(it == currentOption, null)
                Text(stringResource(it.titleId), modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
private fun ListQuestFormPreview() {
    var selectedOption = remember { mutableStateOf(TextItem(DEBIT_AND_CREDIT, R.string.quest_accepts_cards_debit_and_credit)) }
    ListQuestForm(
        listOf(
            TextItem(DEBIT_AND_CREDIT, R.string.quest_accepts_cards_debit_and_credit),
            TextItem(CREDIT_CARDS_ONLY, R.string.quest_accepts_cards_credit_only),
            TextItem(DEBIT_CARDS_ONLY, R.string.quest_accepts_cards_dedit_only),
            TextItem(NEITHER_DEBIT_NOR_CREDIT, R.string.quest_accepts_cards_unavailable),
        ),
        { selectedOption.value = it },
        selectedOption.value
    )
}
