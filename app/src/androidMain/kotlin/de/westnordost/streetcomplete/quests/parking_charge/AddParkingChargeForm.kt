package de.westnordost.streetcomplete.quests.parking_charge

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.util.content

class AddParkingChargeForm : AbstractOsmQuestForm<ParkingChargeAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var amountState: MutableState<String>
    private lateinit var timeUnitState: MutableState<TimeUnit>
    private lateinit var showDialogState: MutableState<Boolean>

    override val otherAnswers: List<AnswerItem> get() = listOf(
        AnswerItem(R.string.quest_parking_charge_varies) {
            showDialogState.value = true
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            Surface {
                amountState = rememberSaveable { mutableStateOf("") }
                timeUnitState = rememberSaveable { mutableStateOf(TimeUnit.HOUR) }
                showDialogState = rememberSaveable { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = getString(R.string.quest_parking_charge_question),
                        style = MaterialTheme.typography.h6
                    )

                    // Amount input with currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountState.value,
                            onValueChange = {
                                amountState.value = it
                                checkIsFormComplete()
                            },
                            label = { Text(getString(R.string.quest_parking_charge_amount_label)) },
                            placeholder = { Text("1.50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(4f),
                            singleLine = true
                        )

                        Text(
                            text = getCurrencySymbol(getCurrencyForCountry()),
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = getString(R.string.quest_parking_charge_time_unit_label),
                            style = MaterialTheme.typography.body1
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Time unit selection

                            DropdownButton(
                                items = TimeUnit.entries,
                                selectedItem = timeUnitState.value,
                                onSelectedItem = { unit ->
                                    timeUnitState.value = unit
                                    checkIsFormComplete()
                                },
                                itemContent = { unit ->
                                    Text(unit.getDisplayName(this@AddParkingChargeForm))
                                },
                                // modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                if (showDialogState.value) {
                    TextInputDialog(
                        onDismissRequest = { showDialogState.value = false },
                        onConfirmed = { description ->
                            applyAnswer(ItVaries(description))
                        },
                        title = { Text(getString(R.string.quest_parking_charge_varies_title)) },
                        textInputLabel = { Text(getString(R.string.quest_parking_charge_varies_description)) }
                    )
                }
            }
        }
    }

    override fun isFormComplete(): Boolean {
        val amount = amountState.value.replace(',', '.')
        return amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0
    }

    override fun onClickOk() {
        val amount = amountState.value.replace(',', '.')
        val currency = getCurrencyForCountry()
        val timeUnit = timeUnitState.value.toOsmValue()

        applyAnswer(SimpleCharge(amount, currency, timeUnit))
    }

    private fun getCurrencyForCountry(): String = when (countryInfo.countryCode) {
        "AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "IE",
        "IT", "LT", "LU", "LV", "MT", "NL", "PT", "SI", "SK", "HR" -> "EUR"
        "GB" -> "GBP"
        "US" -> "USD"
        "CH" -> "CHF"
        "DK" -> "DKK"
        "SE" -> "SEK"
        "NO" -> "NOK"
        "PL" -> "PLN"
        "CZ" -> "CZK"
        "HU" -> "HUF"
        "RO" -> "RON"
        "BG" -> "BGN"
        "JP" -> "JPY"
        "CN" -> "CNY"
        "IN" -> "INR"
        "AU" -> "AUD"
        "CA" -> "CAD"
        else -> "EUR"
    }
}

private fun getCurrencySymbol(currency: String): String = when (currency) {
    "EUR" -> "€"
    "USD" -> "$"
    "GBP" -> "£"
    "JPY" -> "¥"
    else -> currency // otherwise just print out the letters
}

private enum class TimeUnit {
    HOUR,
    DAY,
    MINUTES_30,
    MINUTES_15;

    fun getDisplayName(form: AddParkingChargeForm): String = when (this) {
        HOUR -> form.getString(R.string.quest_parking_charge_per_hour)
        DAY -> form.getString(R.string.quest_parking_charge_per_day)
        MINUTES_30 -> form.getString(R.string.quest_parking_charge_per_30min)
        MINUTES_15 -> form.getString(R.string.quest_parking_charge_per_15min)
    }

    fun toOsmValue(): String = when (this) {
        HOUR -> "hour"
        DAY -> "day"
        MINUTES_30 -> "30 minutes"
        MINUTES_15 -> "15 minutes"
    }
}
