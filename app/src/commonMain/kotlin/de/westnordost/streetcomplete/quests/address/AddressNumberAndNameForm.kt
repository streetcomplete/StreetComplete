package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_address_house_name_label
import de.westnordost.streetcomplete.resources.quest_address_house_number_label
import de.westnordost.streetcomplete.ui.common.Details
import org.jetbrains.compose.resources.stringResource

/** Form to input a housenumber and/or name. If [value]`.name` is null, the name input is not
 *  shown at all, not even retracted,  but only the housenumber input. */
@Composable
fun AddressNumberAndNameForm(
    value: AddressNumberAndName,
    onValueChange: (AddressNumberAndName) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
    houseNumberSuggestion: String? = null,
    blockSuggestion: String? = null,
) {
    val hasNumber = value.number != null
    val hasName = value.name != null
    var numberExpanded by rememberSaveable(hasNumber) { mutableStateOf(hasNumber) }
    var nameExpanded by rememberSaveable(hasName) { mutableStateOf(hasName) }
    val number = value.number ?: HouseNumber("")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (value.name != null) {
            Column {
                Details(
                    expanded = numberExpanded,
                    onExpandedChange = { numberExpanded = it },
                    summary = { Text(stringResource(Res.string.quest_address_house_number_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = value.number?.isEmpty() != false
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AddressNumberForm(
                            value = number,
                            onValueChange = { onValueChange(value.copy(number = it)) },
                            countryCode = countryCode,
                            houseNumberSuggestion = houseNumberSuggestion,
                            blockSuggestion = blockSuggestion,
                        )
                    }
                }
                Details(
                    expanded = nameExpanded,
                    onExpandedChange = { nameExpanded = it },
                    summary = { Text(stringResource(Res.string.quest_address_house_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = value.name.isEmpty()
                ) {
                    TextField(
                        value = value.name,
                        onValueChange = { onValueChange(value.copy(name = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            AddressNumberForm(
                value = number,
                onValueChange = { onValueChange(value.copy(number = it)) },
                countryCode = countryCode,
                houseNumberSuggestion = houseNumberSuggestion,
                blockSuggestion = blockSuggestion,
            )
        }
    }
}
