package de.westnordost.streetcomplete.quests.charge

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.ChargeInput
import de.westnordost.streetcomplete.ui.common.TimeUnit
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.locale.CurrencyFormatElements
import de.westnordost.streetcomplete.util.locale.CurrencyFormatter
import java.util.Currency
import java.util.Locale

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

                val currencyCode = getCurrencyForCountry()
                val currencyFormatInfo = remember(currencyCode) {
                    CurrencyFormatElements.of(currencyCode)
                }

                ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
                    ChargeInput(
                        amount = amountState.value,
                        onAmountChange = {
                            amountState.value = it
                            checkIsFormComplete()
                        },
                        currencyFormatInfo = currencyFormatInfo,
                        timeUnit = timeUnitState.value,
                        onTimeUnitChange = { unit ->
                            timeUnitState.value = unit
                            checkIsFormComplete()
                        },
                        perLabel = getString(R.string.quest_parking_charge_time_unit_label),
                        timeUnitDisplayNames = { unit -> unit.getDisplayName(this@AddParkingChargeForm) },
                        modifier = Modifier.padding(16.dp)
                    )
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

    private fun getCurrencyForCountry(): String = try {
        val locale = Locale.Builder().setRegion(countryInfo.countryCode).build()
        val currency = Currency.getInstance(locale)
        currency.currencyCode
    } catch (_: Exception) {
        "EUR"
    }
}

fun TimeUnit.getDisplayName(form: AddParkingChargeForm): String = when (this) {
    TimeUnit.HOUR -> form.getString(R.string.quest_parking_charge_per_hour)
    TimeUnit.DAY -> form.getString(R.string.quest_parking_charge_per_day)
    TimeUnit.MINUTES_30 -> form.getString(R.string.quest_parking_charge_per_30min)
    TimeUnit.MINUTES_15 -> form.getString(R.string.quest_parking_charge_per_15min)
}
