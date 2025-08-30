package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.util.StringWithCursor

/**
 * parses 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a, "5,5a,6", 7", "5-1" etc. into an
 * appropriate data structure or return null if it cannot be parsed.
 *
 * Ranges are no longer supported or suggested in the app, because they cause ambiguity. (E.g.
 * housenumbers in Korea are very commonly in a format like "5-1" and these are *not* ranges)
 */
fun parseHouseNumbers(string: String): StructuredHouseNumbers? {
    return StructuredHouseNumbers(string.split(",").map { part ->
        parseHouseNumberParts(part) ?: return null
    })
}

private fun parseHouseNumberParts(string: String): StructuredHouseNumber? {
    val c = StringWithCursor(string)
    val houseNumber = c.nextMatchesAndAdvance("\\p{N}{1,5}".toRegex())?.value?.toIntOrNull() ?: return null
    if (c.isAtEnd()) return StructuredHouseNumber.Simple(houseNumber)

    val separatorWithNumber = c.nextMatchesAndAdvance("(\\s?[/-]\\s?)(\\p{N}{1,3})".toRegex())
    if (separatorWithNumber != null) {
        if (!c.isAtEnd()) return null
        return StructuredHouseNumber.WithNumber(
            houseNumber,
            separatorWithNumber.groupValues[1],
            separatorWithNumber.groupValues[2].toInt()
        )
    }

    val separatorWithLetter = c.nextMatchesAndAdvance("(\\s?/?\\s?)(\\p{L})".toRegex())
    if (separatorWithLetter != null) {
        if (!c.isAtEnd()) return null
        return StructuredHouseNumber.WithLetter(
            houseNumber,
            separatorWithLetter.groupValues[1],
            separatorWithLetter.groupValues[2]
        )
    }

    return null
}
