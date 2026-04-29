package de.westnordost.streetcomplete.quests.parcel_locker_brand

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddParcelLockerBrandForm : AbstractOsmQuestForm<String>() {

    @Composable
    override fun Content() {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.parcelLockerBrand,
            onClickOk = { applyAnswer(it) }
        )
    }
}
