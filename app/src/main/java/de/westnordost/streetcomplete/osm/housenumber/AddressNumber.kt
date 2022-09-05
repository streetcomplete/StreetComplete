package de.westnordost.streetcomplete.osm.housenumber

/** The number part of an address, i.e. usually the house number. In some regions, addresses are
 *  instead expressed by conscription numbers or house+block numbers */
sealed interface AddressNumber

data class HouseNumber(val houseNumber: String) : AddressNumber
data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
data class HouseAndBlockNumber(val houseNumber: String, val blockNumber: String) : AddressNumber
