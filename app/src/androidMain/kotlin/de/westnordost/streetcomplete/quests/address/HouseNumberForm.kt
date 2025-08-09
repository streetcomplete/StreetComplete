package de.westnordost.streetcomplete.quests.address

import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Form to input a house number. */
@Composable
fun HouseNumberForm(
    value: HouseNumber,
    onValueChange: (HouseNumber) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: HouseNumber? = null
) {
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
        HouseNumberInput(
            value = value.houseNumber,
            onValueChange = { onValueChange(HouseNumber(it)) },
            modifier = modifier,
            suggestion = suggestion?.houseNumber,
        )
    }
}
