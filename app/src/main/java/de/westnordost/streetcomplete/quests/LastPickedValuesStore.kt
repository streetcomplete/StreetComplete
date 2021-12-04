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

/* Returns the `count` most-common non-null items in the first `historyCount` items of the sequence.
 * If fewer than `count` unique items are found, continues counting items until that many are found,
 * or the end of the sequence is reached. If the first item is not null, it is always included.
 */
fun <T : Any> Sequence<T?>.mostCommonWithin(count: Int, historyCount: Int): Sequence<T> {
    val counts = this.countUniqueNonNull(historyCount, count)
    val top = counts.keys.sortedByDescending { counts[it]!!.count }.take(count)
    val latest = this.take(1).filterNotNull()
    val items = (latest + top).distinct().take(count)
    return items.sortedBy { counts[it]!!.indexOfFirst }
}

private data class ItemStats(val indexOfFirst: Int, var count: Int = 0)

// Counts at least the first `minItems`, keeps going until it finds at least `target` unique values
private fun <T : Any> Sequence<T?>.countUniqueNonNull(minItems: Int, target: Int): Map<T, ItemStats> {
    val counts = mutableMapOf<T, ItemStats>()

    // Sequences are evaluated lazily, so the `forEach` affects each `counts.size` check
    this.withIndex()
        .filter { it.value != null }
        .takeWhile { it.index < minItems || counts.size < target }
        .forEach {
            counts.getOrPut(it.value!!) { ItemStats(it.index) }.count++
        }

    return counts
}

fun <T> Sequence<T>.padWith(defaults: List<T>, count: Int = defaults.size) =
    (this + defaults).distinct().take(count)
