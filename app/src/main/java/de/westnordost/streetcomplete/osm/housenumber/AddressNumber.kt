package de.westnordost.streetcomplete.osm.housenumber

import de.westnordost.streetcomplete.osm.Tags

/** The number part of an address, i.e. usually the house number. In some regions, addresses are
 *  instead expressed by conscription numbers or house+block numbers */
sealed interface AddressNumber

data class HouseNumber(val houseNumber: String) : AddressNumber
data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
data class HouseAndBlockNumber(val houseNumber: String, val blockNumber: String) : AddressNumber

fun AddressNumber.applyTo(tags: Tags) {
    when (this) {
        is ConscriptionNumber -> {
            tags["addr:conscriptionnumber"] = conscriptionNumber
            if (streetNumber != null) {
                tags["addr:streetnumber"] = streetNumber
                tags["addr:housenumber"] = streetNumber
            } else {
                tags["addr:housenumber"] = conscriptionNumber
                tags.remove("addr:streetnumber")
            }
        }
        is HouseAndBlockNumber -> {
            tags["addr:housenumber"] = houseNumber
            tags["addr:block_number"] = blockNumber
        }
        is HouseNumber -> {
            tags["addr:housenumber"] = houseNumber
        }
    }
}

