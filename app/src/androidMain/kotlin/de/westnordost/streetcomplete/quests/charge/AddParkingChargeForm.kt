package de.westnordost.streetcomplete.quests.charge

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.ChargeInput
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.locale.CurrencyFormatElements
import de.westnordost.streetcomplete.util.locale.CurrencyFormatter

class AddParkingChargeForm : AbstractOsmQuestForm<ParkingChargeAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var amountState: MutableState<Double>
    private lateinit var durationUnitState: MutableState<DurationUnit>

    private lateinit var showDialogState: MutableState<Boolean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            Surface {
                amountState = rememberSaveable { mutableDoubleStateOf(0.0) }
                durationUnitState = rememberSaveable { mutableStateOf(DurationUnit.HOURS) }
                showDialogState = rememberSaveable { mutableStateOf(false) }

                val currencyFormatInfo = remember(countryInfo) {
                    CurrencyFormatElements.of(countryInfo.userPreferredLocale)
                }

                ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
                    ChargeInput(
                        amount = amountState.value,
                        onAmountChange = {
                            if (it != null) {
                                amountState.value = it
                            }
                            checkIsFormComplete()
                        },
                        currencyFormatInfo = currencyFormatInfo,
                        durationUnit = durationUnitState.value,
                        onDurationUnitChange = { unit ->
                            durationUnitState.value = unit
                            checkIsFormComplete()
                        },
                        perLabel = getString(R.string.quest_parking_charge_time_unit_label),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    override fun isFormComplete(): Boolean {
        val amount = amountState.value
        return amount != 0.0 && amount > 0.0
    }

    override fun onClickOk() {
        val amount = amountState.value
        val currency = CurrencyFormatter(countryInfo.userPreferredLocale).currencyCode ?: "???"
        applyAnswer(SimpleCharge(amount, currency, durationUnitState.value))
    }
}
