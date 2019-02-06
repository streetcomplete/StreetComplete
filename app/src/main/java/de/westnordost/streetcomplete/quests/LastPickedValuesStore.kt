package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences
import androidx.core.content.edit

import java.util.LinkedList

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.view.Item

/** T must be a string or enum - something that distinctly converts toString. */
class LastPickedValuesStore<T> @Inject constructor(private val prefs: SharedPreferences) {

    fun add(key: String, newValues: Iterable<T>, max: Int = -1) {
        val values = get(key)
        for (value in newValues.map { it.toString() }) {
            values.remove(value)
            values.addFirst(value)
        }
        val lastValues = if (max != -1) values.subList(0, Math.min(values.size, max)) else values
        prefs.edit {
            putString(getKey(key), lastValues.joinToString(","))
        }
    }

    fun add(key: String, value: T, max: Int = -1) {
        add(key, listOf(value), max)
    }

    fun moveLastPickedToFront(key: String, items: LinkedList<Item<T>>, itemPool: List<Item<T>>) {
        val lastPickedItems = find(get(key), itemPool)
        val reverseIt = lastPickedItems.descendingIterator()
        while (reverseIt.hasNext()) {
            val lastPicked = reverseIt.next()
            if (!items.remove(lastPicked)) items.removeLast()
            items.addFirst(lastPicked)
        }
    }

    fun get(key: String): LinkedList<String> {
        val result = LinkedList<String>()
        val values = prefs.getString(getKey(key), null)
        if(values != null) result.addAll(values.split(","))
        return result
    }

    private fun getKey(key: String) = Prefs.LAST_PICKED_PREFIX + key

    private fun find(values: List<String>, itemPool: Iterable<Item<T>>): LinkedList<Item<T>> {
        val result = LinkedList<Item<T>>()
        for (value in values) {
            val item = find(value, itemPool)
            if(item != null) result.add(item)
        }
        return result
    }

    private fun find(value: String, itemPool: Iterable<Item<T>>): Item<T>? {
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
}
