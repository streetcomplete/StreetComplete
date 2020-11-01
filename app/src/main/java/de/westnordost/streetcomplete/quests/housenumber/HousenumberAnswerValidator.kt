package de.westnordost.streetcomplete.quests.housenumber

val VALID_CONSCRIPTIONNUMBER_REGEX = Regex("\\p{N}{1,6}")

// i.e. 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a
val VALID_HOUSENUMBER_REGEX = Regex("\\p{N}{1,5}((\\s?/\\s?\\p{N})|(\\s?/?\\s?\\p{L}))?")

// i.e. 9, 99, 999, 999, 9A, 9 A
val VALID_BLOCKNUMBER_REGEX = Regex("\\p{N}{1,4}(\\s?\\p{L})?")

fun HousenumberAnswer.looksInvalid(additionalValidHousenumberRegex: String?): Boolean {
    val validHousenumberRegex = getValidHouseNumberRegex(additionalValidHousenumberRegex)
    return when(this) {
        is ConscriptionNumber ->
            !number.matches(VALID_CONSCRIPTIONNUMBER_REGEX) || streetNumber != null && !streetNumber.matches(validHousenumberRegex)
        is HouseNumber ->
            !number.matches(validHousenumberRegex)
        is HouseAndBlockNumber ->
            !houseNumber.matches(validHousenumberRegex) || !blockNumber.matches(VALID_BLOCKNUMBER_REGEX)
        else ->
            false
    }
}

// i.e. "95-98" or "5,5a,6" etc. (but not: "1, 3" or "3 - 5" or "5,6-7")
private fun getValidHouseNumberRegex(additionalValidRegex: String?): Regex {
    var regex = VALID_HOUSENUMBER_REGEX.pattern
    if (additionalValidRegex != null) {
        regex = "(($regex)|($additionalValidRegex))"
    }
    return "^$regex((-$regex)|(,$regex)+)?".toRegex()
}