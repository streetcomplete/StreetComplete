package de.westnordost.streetcomplete.util.urlconfig

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import java.math.BigInteger

fun visibleQuestTypesToString(
    visibleQuestTypes: Collection<QuestType>,
    questTypeRegistry: QuestTypeRegistry,
): String =
    Ordinals(visibleQuestTypes.mapNotNull { questTypeRegistry.getOrdinalOf(it) }.toSet())
        .toBooleanArray()
        .toBigInteger()
        .toString(10)

fun stringToVisibleQuestTypes(
    string: String,
    questTypeRegistry: QuestTypeRegistry,
): Collection<QuestType> =
    BigInteger(string, 10)
        .toBooleanArray()
        .toOrdinals()
        .mapNotNull { questTypeRegistry.getByOrdinal(it) }

@JvmInline
value class Ordinals(private val value: Set<Int>): Set<Int> by value

fun Ordinals.toBooleanArray(): BooleanArray =
    BooleanArray(if (isEmpty()) 0 else max() + 1) { contains(it) }

fun BooleanArray.toOrdinals(): Ordinals {
    val ordinals = HashSet<Int>(size)
    for (i in indices) {
        if (this[i]) ordinals.add(i)
    }
    return Ordinals(ordinals)
}

fun BooleanArray.toBigInteger(): BigInteger {
    val str = joinToString(separator = "") { if (it) "1" else "0" }
    if (str.isEmpty()) return 0.toBigInteger()
    // reversed because Java uses big endian, but we want small endian
    return BigInteger(str.reversed(), 2)
}

fun BigInteger.toBooleanArray(): BooleanArray {
    // reversed because Java uses big endian, but we want small endian
    val str = toString(2).reversed()
    if (str == "0") return booleanArrayOf()
    return str.map { it == '1' }.toBooleanArray()
}
