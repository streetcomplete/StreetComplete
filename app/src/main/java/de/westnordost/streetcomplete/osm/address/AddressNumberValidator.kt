package de.westnordost.streetcomplete.osm.address

internal val VALID_CONSCRIPTION_NUMBER_REGEX = Regex("\\p{N}{1,6}")

// e.g. 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a, 99/99
internal val VALID_HOUSE_NUMBER_REGEX = Regex("\\p{N}{1,5}(\\s?/\\s?[\\p{N}\\p{L}]{1,2}|\\s?\\p{L}{1,2})?")

// e.g. 9, 99, 999, 999, 9A, 9 A
internal val VALID_BLOCK_NUMBER_REGEX = Regex("\\p{N}{1,4}(\\s?\\p{L})?")

fun AddressNumber.looksInvalid(additionalValidHouseNumberRegex: String?): Boolean {
    val validHouseNumberRegex = getValidHouseNumberRegex(additionalValidHouseNumberRegex)
    return when (this) {
        is ConscriptionNumber ->
            !conscriptionNumber.matches(VALID_CONSCRIPTION_NUMBER_REGEX) || streetNumber != null && !streetNumber.matches(validHouseNumberRegex)
        is HouseNumber ->
            !houseNumber.matches(validHouseNumberRegex)
        is HouseAndBlockNumber ->
            !houseNumber.matches(validHouseNumberRegex) || !blockNumber.matches(VALID_BLOCK_NUMBER_REGEX)
        is HouseNumberAndBlock ->
            !houseNumber.matches(validHouseNumberRegex)
    }
}

// e.g. "95-98" or "5,5a,6" etc. (but not: "1, 3" or "3 - 5" or "5,6-7")
private fun getValidHouseNumberRegex(additionalValidRegex: String?): Regex {
    var regex = VALID_HOUSE_NUMBER_REGEX.pattern
    if (additionalValidRegex != null) {
        regex = "(($regex)|($additionalValidRegex))"
    }
    return "^$regex((-$regex)|(,$regex)+)?".toRegex()
}
