package de.westnordost.streetcomplete.data.urlconfig

import com.ionspin.kotlin.bignum.integer.BigInteger

@JvmInline
value class Ordinals(private val value: Set<Int>) : Set<Int> by value

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
    if (str.isEmpty()) return BigInteger.ZERO
    // reversed because we want small endian rather than big endian
    return BigInteger.parseString(str.reversed(), 2)
}

fun BigInteger.toBooleanArray(): BooleanArray {
    // reversed because we want small endian rather than big endian
    val str = toString(2).reversed()
    if (str == "0") return booleanArrayOf()
    return str.map { it == '1' }.toBooleanArray()
}

fun String.toBigIntegerOrNull(radix: Int): BigInteger? =
    try {
        BigInteger.parseString(this, radix)
    } catch (e: Exception) {
        null
    }
