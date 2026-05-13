package de.westnordost.streetcomplete.ui.common.quest

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.takeFavorites

class LastPickedChipsRowViewModel(
    val preferences: Preferences,
) : ViewModel() {

    /** Returns at most 5 items where the last picked one at the front, then whichever items
     *  have been picked most */
    inline fun <reified T> getFavorites(key: String): List<T> {
        return preferences.getLastPicked<T>(key)
            .takeFavorites(n = 5, history = 15, first = 1)
    }

    inline fun <reified T> addFavorite(key: String, selection: T) {
        preferences.addLastPicked(key, selection)
    }

    inline fun <reified T> setFavorites(key: String, selection: List<T>) {
        preferences.setLastPicked(key, selection)
    }
}
