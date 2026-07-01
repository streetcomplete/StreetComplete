package de.westnordost.streetcomplete.osm.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Allows to input street or place and (house) number and (house) name*/
@Composable
fun AddressForm(
    value: Address,
    onValueChange: (Address) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
    showStreetOrPlaceSelect: Boolean = true,
    streetNameSuggestion: String? = null,
    placeNameSuggestion: String? = null,
    houseNumberSuggestion: String? = null,
    blockSuggestion: String? = null,
) {
    fun onNumberOrNameChange(number: AddressNumber?, name: String?) {
        var streetOrPlace = value.streetOrPlace
        // apply suggestion
        if (value.streetOrPlace.name.isEmpty()) {
            when (value.streetOrPlace) {
                is PlaceName -> {
                    if (!placeNameSuggestion.isNullOrEmpty()) {
                        streetOrPlace = PlaceName(placeNameSuggestion)
                    }
                }
                is StreetName -> {
                    if (!streetNameSuggestion.isNullOrEmpty()) {
                        streetOrPlace = StreetName(streetNameSuggestion)
                    }
                }
            }
        }
        onValueChange(value.copy(
            streetOrPlace = streetOrPlace,
            number = number,
            name = name,
        ))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StreetOrPlaceNameForm(
            value = value.streetOrPlace,
            onValueChange = { onValueChange(value.copy(streetOrPlace = it)) },
            modifier = Modifier.fillMaxWidth(),
            streetNameSuggestion = streetNameSuggestion,
            placeNameSuggestion = placeNameSuggestion,
            showSelect = showStreetOrPlaceSelect
        )
        AddressNumberAndNameForm(
            number = value.number,
            name = value.name,
            onNumberChange = { onNumberOrNameChange(number = it, name = value.name) },
            onNameChange = { onNumberOrNameChange(number = value.number, name = it) },
            countryCode = countryCode,
            modifier = Modifier.fillMaxWidth(),
            houseNumberSuggestion = houseNumberSuggestion,
            blockSuggestion = blockSuggestion,
        )
    }
}
