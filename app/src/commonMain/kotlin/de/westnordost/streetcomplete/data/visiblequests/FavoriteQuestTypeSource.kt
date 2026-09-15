package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType

interface FavoriteQuestTypeSource {

    /** Interface to be notified of changes in quest type favorites */
    interface Listener {
        fun onFavoriteChanged(quest: QuestType, isFavorite: Boolean)
        /** Called when a number of quest type favorites changed */
        fun onFavoritesChanged()
    }

    /** Return whether the given quest type is favorite */
    fun isFavorite(quest: String, presetId: Long?): Boolean

    fun getFavorites(presetId: Long? = null): List<String>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
