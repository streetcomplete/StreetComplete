package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.util.StringWithCursor

/** parses 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a, "95-98", "5,5a,6", "5d-5f, 7", "5-1" etc. into an appropriate data
 *  structure or return null if it cannot be parsed */
fun parseHouseNumbers(string: String): HouseNumbers? {
    return HouseNumbers(string.split(",").map { part ->
        val range = part.split("-")
        when (range.size) {
            1 -> SingleHouseNumbersPart(parseHouseNumberParts(range[0]) ?: return null)
            2 -> {
                val start = parseHouseNumberParts(range[0]) ?: return null
                val end = parseHouseNumberParts(range[1]) ?: return null
                if (start < end) {
                    HouseNumbersPartsRange(start, end)
                } else {
                    // reverse ranges are interpreted like sub-housenumbers, i.e. 4-2 is about the same as 4/2
                    SingleHouseNumbersPart(parseHouseNumberParts(part) ?: return null)
                }
            }
            else -> return null
        }
    })
}

private fun parseHouseNumberParts(string: String): StructuredHouseNumber? {
    val c = StringWithCursor(string)
    val houseNumber = c.nextMatchesAndAdvance("\\p{N}{1,5}".toRegex())?.value?.toIntOrNull() ?: return null
    if (c.isAtEnd()) return SimpleHouseNumber(houseNumber)

    val separatorWithNumber = c.nextMatchesAndAdvance("(\\s?[/-]\\s?)(\\p{N})".toRegex())
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
