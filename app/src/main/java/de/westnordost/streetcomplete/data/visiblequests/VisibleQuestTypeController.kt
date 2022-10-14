package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import java.util.concurrent.CopyOnWriteArrayList

/** Controller to set/get quest types as enabled or disabled. This controls only the visibility
 *  of quest types per user preference and does not take anything else into account that may
 *  make a quest type invisible (overlays, ...) */
class VisibleQuestTypeController(
    private val visibleQuestTypeDao: VisibleQuestTypeDao,
    private val questPresetsSource: QuestPresetsSource,
    private val questTypeRegistry: QuestTypeRegistry
) : VisibleQuestTypeSource {

    private val listeners = CopyOnWriteArrayList<VisibleQuestTypeSource.Listener>()

    init {
        questPresetsSource.addListener(object : QuestPresetsSource.Listener {
            override fun onSelectedQuestPresetChanged() {
                _visibleQuests = null
                onQuestTypeVisibilitiesChanged()
            }
            override fun onAddedQuestPreset(preset: QuestPreset) {}
            override fun onRenamedQuestPreset(preset: QuestPreset) {}
            override fun onDeletedQuestPreset(presetId: Long) {
                clearVisibilities(presetId)
            }
        })
    }

    /** in-memory cache of visible quests */
    private var _visibleQuests: MutableMap<String, Boolean>? = null
    private val visibleQuests: MutableMap<String, Boolean>
        get() = synchronized(this) {
            if (_visibleQuests == null) {
                _visibleQuests = visibleQuestTypeDao.getAll(selectedPresetId)
            }
            return _visibleQuests!!
        }

    private val selectedPresetId: Long get() = questPresetsSource.selectedId

    fun setVisibility(questType: QuestType, visible: Boolean, presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleQuestTypeDao.put(id, questType.name, visible)
            if (id == selectedPresetId) visibleQuests[questType.name] = visible
        }
        if (id == selectedPresetId) onQuestTypeVisibilityChanged(questType, visible)
    }

    fun setVisibilities(questTypeVisibilities: Map<QuestType, Boolean>, presetId: Long? = null) {
        val questTypeNameVisibilities = questTypeVisibilities
            .filter { it.key !is OsmNoteQuestType }
            .mapKeys { it.key.name }
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleQuestTypeDao.putAll(id, questTypeNameVisibilities)
            if (id == selectedPresetId) questTypeNameVisibilities.forEach { visibleQuests[it.key] = it.value }
        }
        if (id == selectedPresetId) onQuestTypeVisibilitiesChanged()
    }

    fun clearVisibilities(presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleQuestTypeDao.clear(id)
            if (id == selectedPresetId) visibleQuests.clear()
        }
        if (id == selectedPresetId) onQuestTypeVisibilitiesChanged()
    }

    override fun isVisible(questType: QuestType): Boolean =
        visibleQuests[questType.name] ?: (questType.defaultDisabledMessage <= 0)

    override fun getVisible(presetId: Long?): Set<QuestType> {
        val visibilities = visibleQuestTypeDao.getAll(presetId ?: selectedPresetId)

        return questTypeRegistry.filter { questType ->
            visibilities[questType.name] ?: (questType.defaultDisabledMessage <= 0)
        }.toSet()
    }

    override fun addListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.remove(listener)
    }
    private fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) {
        listeners.forEach { it.onQuestTypeVisibilityChanged(questType, visible) }
    }
    private fun onQuestTypeVisibilitiesChanged() {
        listeners.forEach { it.onQuestTypeVisibilitiesChanged() }
    }
}
