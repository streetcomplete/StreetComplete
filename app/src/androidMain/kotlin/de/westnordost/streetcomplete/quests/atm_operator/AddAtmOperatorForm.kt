package de.westnordost.streetcomplete.quests.atm_operator

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddAtmOperatorForm : AbstractOsmQuestForm<String>() {

    @Composable
    override fun Content() {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.atmOperators,
            onClickOk = { applyAnswer(it) }
        )
    }
}
