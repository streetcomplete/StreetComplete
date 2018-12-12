package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.view.Item

fun Array<Item>.sortedBy(names: Iterable<String>): List<Item> {
    val result = mutableListOf(*this)
    // in reverse because the first element in the list should be first in religionsList
    for (name in names.reversed()) {
        for (i in result.indices) {
            val processed = result[i]
            if (processed.value == name) {
                // shuffle to start of list
                result.removeAt(i)
                result.add(0, processed)
                break
            }
        }
    }
    return result
}

