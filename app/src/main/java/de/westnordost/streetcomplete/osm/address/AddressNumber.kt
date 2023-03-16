package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags

/** The number part of an address, i.e. usually the house number. In some regions, addresses are
 *  instead expressed by conscription numbers or house+block numbers */
sealed interface AddressNumber

data class HouseNumber(val houseNumber: String) : AddressNumber
data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
data class HouseAndBlockNumber(val houseNumber: String, val blockNumber: String) : AddressNumber
data class HouseNumberAndBlock(val houseNumber: String, val block: String) : AddressNumber

val AddressNumber.streetHouseNumber: String? get() = when (this) {
    is HouseNumber -> houseNumber
    is HouseAndBlockNumber -> houseNumber
    is HouseNumberAndBlock -> houseNumber
    // not conscription number because there is no logical succession
    else -> null
}

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
        is HouseNumberAndBlock -> {
            tags["addr:housenumber"] = houseNumber
            tags["addr:block"] = block
        }
        is HouseNumber -> {
            tags["addr:housenumber"] = houseNumber
        }
    }
}

fun createAddressNumber(tags: Map<String, String>): AddressNumber? {
    val conscriptionNumber = tags["addr:conscriptionnumber"]
    if (conscriptionNumber != null) {
        val streetNumber = tags["addr:streetnumber"]
        return ConscriptionNumber(conscriptionNumber, streetNumber)
    }
    val houseNumber = tags["addr:housenumber"]
    if (houseNumber != null) {
        val blockNumber = tags["addr:block_number"]
        val block = tags["addr:block"]
        return when {
            blockNumber != null -> HouseAndBlockNumber(houseNumber, blockNumber)
            block != null -> HouseNumberAndBlock(houseNumber, block)
            else -> HouseNumber(houseNumber)
        }
    }
    return null
}
