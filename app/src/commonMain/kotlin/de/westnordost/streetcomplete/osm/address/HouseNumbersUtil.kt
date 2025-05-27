package de.westnordost.streetcomplete.osm.address

/**
 * Assuming that [houseNumber] is a valid house number, adds or subtracts [add] to/from that house
 * number and returns it as a string. Returns `null` if the [houseNumber] cannot be parsed.
 *
 * E.g. adding 1 to a house number like "1c" returns "2". Adding 1 to a house number like "5-7"
 * returns "8" and subtracting 1 from that returns "4".
 * */
fun addToHouseNumber(houseNumber: String, add: Int): String? {
    val parsed = parseHouseNumbers(houseNumber) ?: return null
    val numbers = parsed.list.flatMap {
        when (it) {
            is HouseNumbersPartsRange -> listOf(it.start.number, it.end.number)
            is SingleHouseNumbersPart -> listOf(it.single.number)
        }
    }
    when {
        add == 0 -> return houseNumber
        add > 0 -> {
            val max = numbers.maxOrNull() ?: return null
            return (max + add).toString()
        }
        add < 0 -> {
            val min = numbers.minOrNull() ?: return null
            val result = min + add
            return if (result < 1) null else result.toString()
        }
        else -> return null
    }
}
