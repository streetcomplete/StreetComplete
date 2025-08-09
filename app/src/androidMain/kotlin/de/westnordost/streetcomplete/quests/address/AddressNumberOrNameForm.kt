package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.ui.common.Details

/** Form to input a housenumber and/or name */
@Composable
fun AddressNumberOrNameForm(
    value: AddressNumberOrName,
    onValueChange: (AddressNumberOrName) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Details(
            isInitiallyExpanded = true,
            summary = { Text(stringResource(R.string.quest_address_house_number_label)) },
        ) {
            AddressNumberForm(
                value = value.number ?: HouseNumber(""),
                onValueChange = { onValueChange(value.copy(number = it)) },
                countryCode = countryCode,
            )
        }
        Details(
            isInitiallyExpanded = false,
            summary = { Text(stringResource(R.string.quest_address_house_name_label)) },
        ) {
            TextField(
                value = value.name.orEmpty(),
                onValueChange = { name ->
                    onValueChange(value.copy(name = name.takeIf { it.isNotBlank() }))
                }
            )
        }
    }
}
