package de.westnordost.streetcomplete.ui.common.quest

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.takeFavorites

class ItemSelectViewModel(
    val preferences: Preferences
) : ViewModel() {
    /** Returns [items] but reordered so that the [n] most picked items are at the top */
    inline fun <reified T> getItemsWithFavoritesFirst(key: String, items: List<T>, n: Int): List<T> {
        val favourites = preferences.getLastPicked<T>(key)
            .takeFavorites(n = n)
        return (favourites + items).distinct()
    }

    /** Returns as many items as are in [topItems] but puts 1. the last picked one at the front,
     *  2. then the most picked items and then pads it with [topItems], if applicable */
    inline fun <reified T> getTopItemsWithFavoritesFirst(key: String, topItems: List<T>): List<T> {
        return preferences.getLastPicked<T>(key)
            .takeFavorites(n = topItems.size, first = 1, pad = topItems)
    }

    inline fun <reified T> addFavorite(key: String, selection: T) {
        preferences.addLastPicked(key, selection)
    }

    inline fun <reified T> addFavorites(key: String, selection: List<T>) {
        preferences.addLastPicked(key, selection)
    }
}
