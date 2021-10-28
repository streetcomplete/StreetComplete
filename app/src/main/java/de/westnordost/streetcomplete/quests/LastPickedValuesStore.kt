package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences
import androidx.core.content.edit

import de.westnordost.streetcomplete.Prefs

class LastPickedValuesStore<T : Any>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val serialize: (T) -> String,
    private val deserialize: (String) -> T? // null = unwanted value, see mostCommonWithin
) {
    fun add(newValues: Iterable<T>) {
        val lastValues = newValues.asSequence().map(serialize) + getRaw()
        prefs.edit {
            putString(getKey(), lastValues.take(MAX_ENTRIES).joinToString(","))
        }
    }

    fun add(value: T) = add(listOf(value))

    fun get(): Sequence<T?> = getRaw().map(deserialize)

    private fun getRaw(): Sequence<String> =
        prefs.getString(getKey(), null)?.splitToSequence(",") ?: sequenceOf()

    private fun getKey() = Prefs.LAST_PICKED_PREFIX + key
}

private const val MAX_ENTRIES = 100

/* In the first `historyCount` items, return the `count` most-common non-null items, in order.
 * If the first item is not included (and is not null), it replaces the last of the common items.
 * If fewer than `count` unique items are found, continue counting items until that many are found,
 * or the end of the sequence is reached.
 */
fun <T : Any> Sequence<T?>.mostCommonWithin(count: Int, historyCount: Int): Sequence<T> {
    val counts = this.countUniqueNonNull(historyCount, count)
    val top = counts.keys.sortedByDescending { counts.get(it) }
    val latest = this.take(1).filterNotNull()
    val items = (latest + top).distinct().take(count)
    return items.sortedByDescending { counts.get(it) }
}

// Counts at least the first `minItems`, keeps going until it finds at least `target` unique values
private fun <T : Any> Sequence<T?>.countUniqueNonNull(minItems: Int, target: Int): Map<T, Int> {
    val counts = mutableMapOf<T, Int>()
    val items = takeAtLeastWhile(minItems) { counts.size < target }.filterNotNull()
    return items.groupingBy { it }.eachCountTo(counts)
}

// Take at least `count` elements, then continue until `predicate` returns false
private fun <T> Sequence<T>.takeAtLeastWhile(count: Int, predicate: (T) -> Boolean): Sequence<T> =
    withIndex().takeWhile{ (i, t) -> i < count || predicate(t) }.map { it.value }

fun <T> Sequence<T>.padWith(defaults: List<T>, count: Int = defaults.size) =
    (this + defaults).distinct().take(count)
