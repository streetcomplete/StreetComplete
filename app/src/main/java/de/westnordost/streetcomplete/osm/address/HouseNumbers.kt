package de.westnordost.streetcomplete.osm.address

/** Structured representation of a addr:housenumber string */
data class HouseNumbers(val list: List<HouseNumbersPart>) {
    override fun toString() = list.joinToString(",")
}

/** -------------------------------------- HouseNumbersPart ------------------------------------- */

sealed class HouseNumbersPart : Comparable<HouseNumbersPart> {
    override fun compareTo(other: HouseNumbersPart): Int = when (this) {
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
    numbers.all { it < 0 } -> -1
    else -> 0
}

/** e.g. 5a-12, 100-20/1*/
data class HouseNumbersPartsRange(
    val start: StructuredHouseNumber,
    val end: StructuredHouseNumber
) : HouseNumbersPart() {
    override fun toString() = "$start-$end"
}

data class SingleHouseNumbersPart(val single: StructuredHouseNumber) : HouseNumbersPart() {
    override fun toString() = "$single"
}

/** ----------------------------------- StructuredHouseNumber ---------------------------------- */

sealed class StructuredHouseNumber : Comparable<StructuredHouseNumber> {
    abstract val number: Int

    override fun compareTo(other: StructuredHouseNumber): Int {
        val diffNumber = number - other.number
        if (diffNumber != 0) return diffNumber
        when {
            this is HouseNumberWithLetter && other is HouseNumberWithLetter -> {
                if (letter < other.letter) {
                    return -1
                } else if (letter > other.letter) {
                    return +1
                }
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
