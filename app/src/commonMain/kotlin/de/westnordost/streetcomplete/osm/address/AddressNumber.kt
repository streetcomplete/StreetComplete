package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags
import kotlinx.serialization.Serializable

/** The number part of an address, i.e. usually the house number. In some regions, addresses are
 *  instead expressed by conscription numbers or house+block numbers */
@Serializable
sealed interface AddressNumber {
    fun isEmpty(): Boolean = when (this) {
        is BlockAndHouseNumber -> block.isEmpty() && houseNumber.isEmpty()
        is ConscriptionNumber -> conscriptionNumber.isEmpty() && streetNumber.isNullOrEmpty()
        is HouseNumber -> houseNumber.isEmpty()
    }

    fun isComplete(): Boolean = when (this) {
        is BlockAndHouseNumber -> block.isNotEmpty() && houseNumber.isNotEmpty()
        is ConscriptionNumber -> conscriptionNumber.isNotEmpty()
        is HouseNumber -> houseNumber.isNotEmpty()
    }
}

@Serializable
data class HouseNumber(val houseNumber: String) : AddressNumber
@Serializable
data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
@Serializable
data class BlockAndHouseNumber(val block: String, val houseNumber: String) : AddressNumber

val AddressNumber.streetHouseNumber: String? get() = when (this) {
    is HouseNumber -> houseNumber
    is BlockAndHouseNumber -> houseNumber
    is ConscriptionNumber -> streetNumber
}

fun AddressNumber.applyTo(tags: Tags, countryCode: String?) {
    // first, clear all...
    listOf(
        "addr:housenumber",
        "addr:conscriptionnumber",
        "addr:streetnumber",
        "addr:block_number",
        "addr:block"
    ).forEach { tags.remove(it) }

    if (!isComplete()) return

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
        is BlockAndHouseNumber -> {
            tags["addr:housenumber"] = houseNumber
            if (countryCode == "JP") tags["addr:block_number"] = block
            else tags["addr:block"] = block
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
            blockNumber != null -> BlockAndHouseNumber(blockNumber, houseNumber)
            block != null -> BlockAndHouseNumber(block, houseNumber)
            else -> HouseNumber(houseNumber)
        }
    }
    return null
}
