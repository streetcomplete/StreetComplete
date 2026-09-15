package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.util.Listeners

/** Controls which quest types have been set to favorite by the user */
class FavoriteQuestTypeController(
    private val favoriteQuestTypeDao: FavoriteQuestTypeDao,
    private val editTypePresetsSource: EditTypePresetsSource
) : FavoriteQuestTypeSource {
    private val listeners = Listeners<FavoriteQuestTypeSource.Listener>()

    private val selectedPresetId: Long get() = editTypePresetsSource.selectedId

    init {
        editTypePresetsSource.addListener(object : EditTypePresetsSource.Listener {
            override fun onSelectionChanged() {
                onFavoriteQuestTypesChanged()
            }
            override fun onAdded(preset: EditTypePreset) {}
            override fun onRenamed(preset: EditTypePreset) {}
            override fun onDeleted(presetId: Long) {
                clear(presetId)
            }
        })
    }

    fun setFavorite(
        questType: QuestType,
        isFavorite: Boolean,
        presetId: Long? = null
    ) {
        val id = presetId ?: selectedPresetId
        if (isFavorite) favoriteQuestTypeDao.put(id, questType.name)
        else favoriteQuestTypeDao.remove(id, questType.name)
        if (id == selectedPresetId) onFavoriteQuestTypeChanged(questType, isFavorite)
    }

    fun copyFavorites(fromPresetId: Long, toPresetId: Long) {
        val favorites = favoriteQuestTypeDao.getAll(fromPresetId)
        favoriteQuestTypeDao.putAll(toPresetId, favorites)
    }

    override fun getFavorites(presetId: Long?): List<String> {
        val id = presetId ?: selectedPresetId
        return favoriteQuestTypeDao.getAll(id)
    }

    override fun isFavorite(quest: String, presetId:Long?): Boolean {
        val id = presetId ?: selectedPresetId
        return favoriteQuestTypeDao.get(id, quest) != null
    }

    fun clear(presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        favoriteQuestTypeDao.clear(id)
        if (id == selectedPresetId) onFavoriteQuestTypesChanged()
    }

    override fun addListener(listener: FavoriteQuestTypeSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: FavoriteQuestTypeSource.Listener) {
        listeners.remove(listener)
    }
    private fun onFavoriteQuestTypeChanged(
        questType: QuestType,
        isFavorite: Boolean
    ) {
        listeners.forEach {
            it.onFavoriteChanged(questType, isFavorite)
        }
    }
    private fun onFavoriteQuestTypesChanged() {
        listeners.forEach {
            it.onFavoritesChanged()
        }
    }
}
