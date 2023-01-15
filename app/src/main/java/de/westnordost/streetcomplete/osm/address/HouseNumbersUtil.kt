package de.westnordost.streetcomplete.osm.address

fun addToHouseNumber(houseNumber: String, add: Int): String? {
    val parsed = parseHouseNumbers(houseNumber) ?: return null
    val numbers = parsed.list.flatMap {
        when (it) {
            is HouseNumbersPartsRange -> listOf(it.start.number, it.end.number)
            is SingleHouseNumbersPart -> listOf(it.single.number)
        }
    }
    val p = parsed.list.singleOrNull()
    val firstPart = if (p is SingleHouseNumbersPart && p.single is HouseNumberWithLetterAtStart)
        p.single.letter + p.single.separator
        else ""
    when {
        add == 0 -> return houseNumber
        add > 0 -> {
            val max = numbers.maxOrNull() ?: return null
            return firstPart + (max + add)
        }
        add < 0 -> {
            val min = numbers.minOrNull() ?: return null
            val result = min + add
            return if (result < 1) null else firstPart + result
        }
        else -> return null
    }
}
