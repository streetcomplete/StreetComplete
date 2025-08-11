package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    houseNumberSuggestion: String? = null,
    blockSuggestion: String? = null,
) {
    Column(modifier = modifier) {
        val hasNumberInput = value.number?.isEmpty() == false
        Details(
            expanded = true or hasNumberInput,
            summary = { Text(stringResource(R.string.quest_address_house_number_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !hasNumberInput
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AddressNumberForm(
                    value = value.number ?: HouseNumber(""),
                    onValueChange = { onValueChange(value.copy(number = it)) },
                    countryCode = countryCode,
                    houseNumberSuggestion = houseNumberSuggestion,
                    blockSuggestion = blockSuggestion,
                )
            }

        }
        val hasNameInput = value.name?.isEmpty() == false
        Details(
            expanded = false or hasNameInput,
            summary = { Text(stringResource(R.string.quest_address_house_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !hasNameInput
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
