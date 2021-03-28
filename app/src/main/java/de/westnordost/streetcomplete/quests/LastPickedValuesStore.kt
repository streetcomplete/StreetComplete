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

    fun add(key: String, newValues: Iterable<T>, max: Int = -1) {
        val values = get(key)
        for (value in newValues.map { it.toString() }) {
            values.remove(value)
            values.addFirst(value)
        }
        val lastValues = if (max != -1) values.subList(0, min(values.size, max)) else values
        prefs.edit {
            putString(getKey(key), lastValues.joinToString(","))
        }
    }

    fun add(key: String, value: T, max: Int = -1) {
        add(key, listOf(value), max)
    }

    fun get(key: String): LinkedList<String> {
        val result = LinkedList<String>()
        val values = prefs.getString(getKey(key), null)
        if(values != null) result.addAll(values.split(","))
        return result
    }

    private fun getKey(key: String) = Prefs.LAST_PICKED_PREFIX + key
}

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
