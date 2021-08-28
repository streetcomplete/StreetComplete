package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.data.elementfilter.StringWithCursor

/** parses 99999/a, 9/a, 99/9, 99a, 99 a, 9 / a, "95-98", "5,5a,6", "5d-5f, 7", "5-1" etc. into an appropriate data
 *  structure or return null if it cannot be parsed */
fun parseHouseNumber(string: String): HouseNumbers? {
    return HouseNumbers(string.split(",").map { part ->
        val range = part.split("-")
        when (range.size) {
            1 -> SingleHouseNumbersPart(parseHouseNumberParts(range[0]) ?: return null)
            2 -> {
                val start = parseHouseNumberParts(range[0]) ?: return null
                val end = parseHouseNumberParts(range[1]) ?: return null
                if (start < end) {
                    HouseNumbersPartsRange(start, end)
                }
                // reverse ranges are interpreted like sub-housenumbers, i.e. 4-2 is about the same as 4/2
                else {
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

data class HouseNumbers(val list: List<HouseNumbersPart>) {
    override fun toString() = list.joinToString(",")
}

/** -------------------------------------- HouseNumbersPart ------------------------------------- */

sealed class HouseNumbersPart: Comparable<HouseNumbersPart> {
    override fun compareTo(other: HouseNumbersPart): Int = when(this) {
        is SingleHouseNumbersPart -> when (other) {
            // e.g. 12c < 15
            is SingleHouseNumbersPart -> sign(
                single.compareTo(other.single)
            )
            // e.g. 10 < 12-14, but 10 ≮ 8-12 (and also not bigger than)
            is HouseNumbersPartsRange -> sign(
                single.compareTo(other.start),
                single.compareTo(other.end)
            )
        }
        is HouseNumbersPartsRange -> when (other) {
            // e.g. 12-14 < 16a, but 12-14 ≮ 13 (and also not bigger than)
            is SingleHouseNumbersPart -> sign(
                start.compareTo(other.single),
                end.compareTo(other.single)
            )
            // e.g. 8-14 < 16-18 but not if the ranges intersect
            is HouseNumbersPartsRange -> sign(
                start.compareTo(other.start),
                start.compareTo(other.end),
                end.compareTo(other.start),
                end.compareTo(other.end)
            )
        }
    }
}

private fun sign(vararg numbers: Int): Int = when {
    numbers.all { it > 0 } -> 1
    numbers.all { it < 0} -> -1
    else -> 0
}

/** e.g. 5a-12, 100-20/1*/
data class HouseNumbersPartsRange(
    val start: StructuredHouseNumber,
    val end: StructuredHouseNumber
): HouseNumbersPart() {
    override fun toString() = "$start-$end"
}

data class SingleHouseNumbersPart(val single: StructuredHouseNumber) : HouseNumbersPart() {
    override fun toString() = "$single"
}

/** ----------------------------------- StructuredHouseNumber ---------------------------------- */

sealed class StructuredHouseNumber: Comparable<StructuredHouseNumber> {
    abstract val number: Int

    override fun compareTo(other: StructuredHouseNumber): Int {
        val diffNumber = number - other.number
        if (diffNumber != 0) return diffNumber
        when {
            this is HouseNumberWithLetter && other is HouseNumberWithLetter -> {
                if (letter < other.letter) return -1
                else if (letter > other.letter) return +1
            }
            this is HouseNumberWithNumber && other is HouseNumberWithNumber -> {
                val diffNumber2 = number2 - other.number2
                if (diffNumber2 != 0) return diffNumber2
            }
        }
        return 0
    }
}

/** e.g. 12 */
data class SimpleHouseNumber(override val number: Int) : StructuredHouseNumber() {
    override fun toString() = number.toString()
}
/** e.g. 12c */
data class HouseNumberWithLetter(
    override val number: Int,
    val separator: String,
    val letter: String
) : StructuredHouseNumber() {
    override fun toString() = "$number$separator$letter"
}
/** e.g. 12/3 */
data class HouseNumberWithNumber(
    override val number: Int,
    val separator: String,
    val number2: Int
) : StructuredHouseNumber() {
    override fun toString() = "$number$separator$number2"
}
