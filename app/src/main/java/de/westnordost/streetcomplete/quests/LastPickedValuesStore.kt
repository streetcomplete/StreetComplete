package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences

import java.util.LinkedList

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.view.GroupedItem

class LastPickedValuesStore @Inject constructor(private val prefs: SharedPreferences) {

    fun add(key: String, newValues: Iterable<String>, max: Int = -1) {
        val values = get(key)
        for (value in newValues) {
            values.remove(value)
            values.addFirst(value)
        }
        val lastValues = if (max != -1) values.subList(0, Math.min(values.size, max)) else values
        prefs.edit().putString(getKey(key), lastValues.joinToString(",")).apply()
    }

    fun add(key: String, value: String, max: Int = -1) {
        add(key, listOf(value), max)
    }

    fun moveLastPickedToFront(key: String, items: LinkedList<GroupedItem>, itemPool: List<GroupedItem>) {
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

    private fun find(values: List<String>, itemPool: Iterable<GroupedItem>): LinkedList<GroupedItem> {
        val result = LinkedList<GroupedItem>()
        for (value in values) {
            val item = find(value, itemPool)
            if(item != null) result.add(item)
        }
        return result
    }

    private fun find(value: String, itemPool: Iterable<GroupedItem>): GroupedItem? {
        for (item in itemPool) {
            val subItems = item.items
            // returns only items which are not groups themselves
            if (subItems != null) {
                val subItem = find(value, subItems.asIterable())
                if (subItem != null) return subItem
            } else if (value == item.value) {
                return item
            }
        }
        return null
    }
}
