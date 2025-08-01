package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags

/** The number part of an address, i.e. usually the house number. In some regions, addresses are
 *  instead expressed by conscription numbers or house+block numbers */
sealed interface AddressNumber

data class HouseNumber(val houseNumber: String) : AddressNumber
data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
data class BlockNumberAndHouseNumber(val blockNumber: String, val houseNumber: String) : AddressNumber
data class BlockAndHouseNumber(val block: String, val houseNumber: String) : AddressNumber

val AddressNumber.streetHouseNumber: String? get() = when (this) {
    is HouseNumber -> houseNumber
    is BlockNumberAndHouseNumber -> houseNumber
    is BlockAndHouseNumber -> houseNumber
    // not conscription number because there is no logical succession
    else -> null
}

fun AddressNumber.applyTo(tags: Tags) {
    // first, clear all...
    listOf(
        "addr:housenumber",
        "addr:conscriptionnumber",
        "addr:streetnumber",
        "addr:block_number",
        "addr:block"
    ).forEach { tags.remove(it) }

    when (this) {
        is ConscriptionNumber -> {
            tags["addr:conscriptionnumber"] = conscriptionNumber
            if (!streetNumber.isNullOrEmpty()) {
                tags["addr:streetnumber"] = streetNumber
                tags["addr:housenumber"] = streetNumber
            } else {
                tags["addr:housenumber"] = conscriptionNumber
            }
        }
        is BlockNumberAndHouseNumber -> {
            tags["addr:housenumber"] = houseNumber
            tags["addr:block_number"] = blockNumber
        }
        is BlockAndHouseNumber -> {
            tags["addr:housenumber"] = houseNumber
            tags["addr:block"] = block
        }
        is HouseNumber -> {
            tags["addr:housenumber"] = houseNumber
        }
    }
}

fun parseAddressNumber(tags: Map<String, String>): AddressNumber? {
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
            blockNumber != null -> BlockNumberAndHouseNumber(blockNumber, houseNumber)
            block != null -> BlockAndHouseNumber(block, houseNumber)
            else -> HouseNumber(houseNumber)
        }
    }
    return null
}
