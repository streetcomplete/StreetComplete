package de.westnordost.streetcomplete.quests.charging_station_operator

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddChargingStationOperatorForm : AbstractOsmQuestForm<String>() {

    @Composable
    override fun Content() {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.chargingStationOperators,
            onClickOk = { applyAnswer(it) }
        )
    }
}
