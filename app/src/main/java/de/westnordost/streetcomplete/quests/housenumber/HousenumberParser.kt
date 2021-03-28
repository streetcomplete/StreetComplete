package de.westnordost.streetcomplete.quests.housenumber

import de.westnordost.streetcomplete.data.elementfilter.StringWithCursor

/** parses 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a, "95-98", "5,5a,6", "5d-5f, 7" etc. into an appropriate data
 *  structure or return null if it cannot be parsed */
fun parseHouseNumber(string: String): HouseNumbers? {
    return HouseNumbers(string.split(",").map { part ->
        val range = part.split("-")
        when (range.size) {
            1 -> SingleHouseNumbersPart(parseHouseNumberParts(range[0]) ?: return null)
            2 -> {
                HouseNumbersPartsRange(
                    parseHouseNumberParts(range[0]) ?: return null,
                    parseHouseNumberParts(range[1]) ?: return null
                )
            }
            else -> return null
        }
    })
}

private fun parseHouseNumberParts(string: String): StructuredHouseNumber? {
    val c = StringWithCursor(string)
    val houseNumber = c.nextMatchesAndAdvance("\\p{N}{1,5}".toRegex())?.value?.toIntOrNull() ?: return null
    if (c.isAtEnd()) return SimpleHouseNumber(houseNumber)

    val separatorWithNumber = c.nextMatchesAndAdvance("(\\s?/\\s?)(\\p{N})".toRegex())
    if (separatorWithNumber != null) {
        if (!c.isAtEnd()) return null
        return HouseNumberWithNumber(
            houseNumber,
            separatorWithNumber.groupValues[1],
            separatorWithNumber.groupValues[2].toInt()
        )
    }

    val separatorWithLetter = c.nextMatchesAndAdvance("(\\s?/?\\s?)(\\p{L})".toRegex())
    if (separatorWithLetter != null) {
        if (!c.isAtEnd()) return null
        return HouseNumberWithLetter(
            houseNumber,
            separatorWithLetter.groupValues[1],
            separatorWithLetter.groupValues[2]
        )
    }

    return null
}

data class HouseNumbers(val list: List<HouseNumbersPart>) {
    override fun toString() = list.joinToString(",")
}

sealed class HouseNumbersPart

data class HouseNumbersPartsRange(
    val start: StructuredHouseNumber,
    val end: StructuredHouseNumber
): HouseNumbersPart() {
    override fun toString() = "$start-$end"
}

data class SingleHouseNumbersPart(val single: StructuredHouseNumber) : HouseNumbersPart() {
    override fun toString() = "$single"
}

sealed class StructuredHouseNumber {
    abstract val number: Int
}

data class SimpleHouseNumber(override val number: Int) : StructuredHouseNumber() {
    override fun toString() = number.toString()
}
data class HouseNumberWithLetter(
    override val number: Int,
    val separator: String,
    val letter: String
) : StructuredHouseNumber() {
    override fun toString() = "$number$separator$letter"
}
data class HouseNumberWithNumber(
    override val number: Int,
    val separator: String,
    val number2: Int
) : StructuredHouseNumber() {
    override fun toString() = "$number$separator$number2"
}