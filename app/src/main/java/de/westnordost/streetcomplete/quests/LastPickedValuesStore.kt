package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences
import androidx.core.content.edit

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import kotlin.math.min

class LastPickedValuesStore<T : Any> @Inject constructor(private val prefs: SharedPreferences) {

    fun add(factory: Factory<T>, newValues: Iterable<T>) {
        val lastValues = newValues.asSequence().map(factory::serialize) + getRaw(factory.key)
        prefs.edit {
            putString(getKey(factory.key), lastValues.take(MAX_ENTRIES).joinToString(","))
        }
    }

    fun add(factory: Factory<T>, value: T) = add(factory, listOf(value))

    fun get(factory: Factory<T>): Sequence<T?> = getRaw(factory.key).map(factory::deserialize)

    private fun getRaw(key: String): Sequence<String> =
        prefs.getString(getKey(key), null)?.splitToSequence(",") ?: sequenceOf()

    private fun getKey(key: String) = Prefs.LAST_PICKED_PREFIX + key

    interface Factory<T : Any> {
        val key: String
        fun serialize(item: T): String
        fun deserialize(value: String): T? // null = invalid value
    }
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
