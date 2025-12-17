package de.westnordost.streetcomplete.quests.parking_charge

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddParkingChargeForm : AbstractOsmQuestForm<ParkingChargeAnswer>() {

    override val contentLayoutResId = R.layout.quest_parking_charge

    override val otherAnswers: List<AnswerItem> get() = listOf(
        AnswerItem(R.string.quest_parking_charge_varies) {
            showItVariesDialog()
        }
    )

    private var amountInput: String? = null
    private var timeValue: Int = 60  // Default: 60 Minuten

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize UI-Elements
        // - EditText für den Betrag
        // - Spinner/Buttons für die Zeiteinheit
        // - Währung wird automatisch aus dem Land bestimmt
    }

    override fun onClickOk() {
        val amount = amountInput ?: return
        val currency = getCurrencyForCountry()
        val timeUnit = formatTimeUnit(timeValue)

        applyAnswer(SimpleCharge(amount, currency, timeUnit))
    }

    override fun isFormComplete(): Boolean = amountInput != null && amountInput!!.toDoubleOrNull() != null

    private fun getCurrencyForCountry(): String {
        // TODO: Implement logic for determining currency based on countryInfo.countryCode
        return when (countryInfo.countryCode) {
            "DE", "FR", "IT" -> "EUR"
            "GB" -> "GBP"
            "US" -> "USD"
            else -> "EUR"  // Fallback
        }
    }

    private fun formatTimeUnit(minutes: Int): String = when {
            minutes == 60 -> "hour"
            minutes % 60 == 0 -> "${minutes / 60} hours"
            else -> "$minutes minutes"
        }

    private fun showItVariesDialog() {
        // TODO: Show Dialog for charge:description
    }
}
