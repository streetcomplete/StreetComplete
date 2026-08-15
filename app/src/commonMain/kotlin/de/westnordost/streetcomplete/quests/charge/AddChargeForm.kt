package de.westnordost.streetcomplete.quests.charge

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ChargeInput
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.formatPadded
import de.westnordost.streetcomplete.util.locale.CurrencyFormatElements
import de.westnordost.streetcomplete.util.locale.CurrencyFormatter
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddChargeForm(
    on: (QuestAction<Charge>) -> Unit,
    countryInfo: CountryInfo,
) {
    var amount by rememberSerializable { mutableStateOf<Double?>(null) }
    var durationUnit by rememberSerializable { mutableStateOf(DurationUnit.HOURS) }

    val currencyFormatInfo = remember(countryInfo) {
        CurrencyFormatElements.of(countryInfo.userPreferredLocale)
    }

    QuestForm(
        on = on,
        isComplete = amount != null && amount != 0.0,
        onClickOk = {
            val amount = amount!!
            val currency = CurrencyFormatter(countryInfo.userPreferredLocale).currencyCode ?: "¤"
            on(Answer(Charge(
                amount.formatPadded(currencyFormatInfo.decimalDigits, true),
                currency,
                durationUnit
            )))
        }
    ) {
        ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
            ChargeInput(
                amount = amount,
                onAmountChange = { amount = it },
                currencyFormatInfo = currencyFormatInfo,
                durationUnit = durationUnit,
                onDurationUnitChange = { durationUnit = it },
                perLabel = stringResource(Res.string.quest_parking_charge_time_unit_label),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
