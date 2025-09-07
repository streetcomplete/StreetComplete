package de.westnordost.streetcomplete.quests.address

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.ConscriptionNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber

/** Form to input a housenumber or housenumber + block or conscription number + orientation number,
 *  depending on country. */
@Composable
fun AddressNumberForm(
    value: AddressNumber,
    onValueChange: (AddressNumber) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
    houseNumberSuggestion: String? = null,
    blockSuggestion: String? = null,
) {
    val blockAndHouseNumberSuggestion = remember(blockSuggestion, houseNumberSuggestion) {
        if (houseNumberSuggestion != null || blockSuggestion != null) {
            BlockAndHouseNumber(blockSuggestion.orEmpty(), houseNumberSuggestion.orEmpty())
        } else null
    }

    var value = value
    // Czechia, Slovakia: always special conscription number form
    if (countryCode == "CZ" || countryCode == "SK") {
        if (value !is ConscriptionNumber) value = ConscriptionNumber("")
    }
    // Japan: always block + house number
    if (countryCode == "JP") {
        if (value !is BlockAndHouseNumber) value = BlockAndHouseNumber("", "")
    }

    when (value) {
        is ConscriptionNumber -> ConscriptionNumberForm(
            value = value,
            onValueChange = onValueChange,
            countryCode = countryCode,
            modifier = modifier,
        )
        is BlockAndHouseNumber -> BlockAndHouseNumberForm(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            suggestion = blockAndHouseNumberSuggestion,
        )
        is HouseNumber -> HouseNumberForm(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            suggestion = houseNumberSuggestion?.let { HouseNumber(it) }
        )
    }
}
