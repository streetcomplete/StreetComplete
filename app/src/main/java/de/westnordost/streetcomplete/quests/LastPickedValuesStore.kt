package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences
import androidx.core.content.edit

import java.util.LinkedList

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import kotlin.math.min

/** T must be a string or enum - something that distinctly converts toString. */
class LastPickedValuesStore<T> @Inject constructor(private val prefs: SharedPreferences) {

    fun add(key: String, newValues: Iterable<T>, max: Int? = null, allowDuplicates: Boolean = false) {
        val values = get(key)
        for (value in newValues.map { it.toString() }) {
            values.addFirst(value)
        }
        val unique = if (allowDuplicates) values else values.distinct()
        val lastValues = unique.subList(0, min(unique.size, max ?: MAX_ENTRIES))
        android.util.Log.d("FAV", "Pref ${key} value: ${lastValues.joinToString(",")}")
        prefs.edit {
            putString(getKey(key), lastValues.joinToString(","))
        }
    }

    fun add(key: String, value: T, max: Int? = null, allowDuplicates: Boolean = false) {
        add(key, listOf(value), max, allowDuplicates)
    }

    fun get(key: String): LinkedList<String> {
        val result = LinkedList<String>()
        val values = prefs.getString(getKey(key), null)
        if(values != null) result.addAll(values.split(","))
        return result
    }

    private fun getKey(key: String) = Prefs.LAST_PICKED_PREFIX + key
}

private const val MAX_ENTRIES = 100

/* Returns `count` unique items, sorted by how often they appear in the last `historyCount` answers.
 * If fewer than `count` unique items are found, look farther back in the history.
 * Only returns items in `itemPool` ("valid"), although other answers count towards `historyCount`.
 * If there are not enough unique items in the whole history, add unique `defaultItems` as needed.
 * Always include the most recent answer, if it is in `itemPool`, but still sorted normally. So, if
 * it is not one of the `count` most frequent items, it will replace the last of those.
 *
 * impl: null represents items not in the item pool
 */
fun <T> LastPickedValuesStore<T>.getWeighted(
    key: String,
    count: Int,
    historyCount: Int,
    defaultItems: List<GroupableDisplayItem<T>>,
    itemPool: List<GroupableDisplayItem<T>>
): List<GroupableDisplayItem<T>> {
    val stringToItem = itemPool.associateBy { it.value.toString() }
    val recents = get(key).asSequence().map { stringToItem.get(it) }
    val counts = recents.countUniqueNonNull(historyCount, count)
    val topRecent = counts.keys.sortedByDescending { counts.get(it) }
    val first = recents.take(1).filterNotNull()
    val items = (first + topRecent + defaultItems).distinct().take(count)
    return items.sortedByDescending { counts.get(it) }.toList()
}

// Counts at least the first `minItems`, keeps going until it finds at least `target` unique values
private fun <T> Sequence<T?>.countUniqueNonNull(minItems: Int, target: Int): Map<T, Int> {
    val counts = mutableMapOf<T, Int>()
    val items = takeAtLeastWhile(minItems) { counts.size < target }.filterNotNull()
    return items.groupingBy { it }.eachCountTo(counts)
}

// Take at least `count` elements, then continue until `predicate` returns false
private fun <T> Sequence<T>.takeAtLeastWhile(count: Int, predicate: (T) -> Boolean): Sequence<T> =
    withIndex().takeWhile{ (i, t) -> i < count || predicate(t) }.map { it.value }

fun <T> LastPickedValuesStore<T>.moveLastPickedGroupableDisplayItemToFront(
    key: String,
    items: LinkedList<GroupableDisplayItem<T>>,
    itemPool: List<GroupableDisplayItem<T>>)
{
    val lastPickedItems = find(get(key), itemPool)
    val reverseIt = lastPickedItems.descendingIterator()
    while (reverseIt.hasNext()) {
        val lastPicked = reverseIt.next()
        if (!items.remove(lastPicked)) items.removeLast()
        items.addFirst(lastPicked)
    }
}

private fun <T> find(values: List<String>, itemPool: Iterable<GroupableDisplayItem<T>>): LinkedList<GroupableDisplayItem<T>> {
    val result = LinkedList<GroupableDisplayItem<T>>()
    for (value in values) {
        val item = find(value, itemPool)
        if(item != null) result.add(item)
    }
    return result
}

private fun <T> find(value: String, itemPool: Iterable<GroupableDisplayItem<T>>): GroupableDisplayItem<T>? {
    for (item in itemPool) {
        val subItems = item.items
        // returns only items which are not groups themselves
        if (subItems != null) {
            val subItem = find(value, subItems.asIterable())
            if (subItem != null) return subItem
        } else if (value == item.value.toString()) {
            return item
        }
    }
    return null
}

fun <T> LastPickedValuesStore<T>.moveLastPickedDisplayItemsToFront(
    key: String,
    items: LinkedList<DisplayItem<T>>,
    itemPool: List<DisplayItem<T>>)
{
    val lastPickedItems = findDisplayItems(get(key), itemPool)
    val reverseIt = lastPickedItems.descendingIterator()
    while (reverseIt.hasNext()) {
        val lastPicked = reverseIt.next()
        if (!items.remove(lastPicked)) items.removeLast()
        items.addFirst(lastPicked)
    }
}

private fun <T> findDisplayItems(values: List<String>, itemPool: Iterable<DisplayItem<T>>): LinkedList<DisplayItem<T>> {
    val result = LinkedList<DisplayItem<T>>()
    for (value in values) {
        val item = itemPool.find { it.value.toString() == value }
        if (item != null) result.add(item)
    }
    return result
}
