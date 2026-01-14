package de.westnordost.streetcomplete.osm.maxstay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.duration.DurationInput
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestrictionInput

/** Form to input the maximum time a vehicle may stay at a parking: A duration plus an optional
 *  time restriction. */
@Composable
fun MaxStayInput(
    maxStay: MaxStay,
    onChange: (MaxStay) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DurationInput(
            duration = maxStay.duration,
            onChange = { onChange(maxStay.copy(duration = it)) },
        )
        TimeRestrictionInput(
            timeRestriction = maxStay.timeRestriction,
            onChange = { onChange(maxStay.copy(timeRestriction = it)) },
            countryInfo = countryInfo,
            allowSelectNoRestriction = true,
        )
    }
}
