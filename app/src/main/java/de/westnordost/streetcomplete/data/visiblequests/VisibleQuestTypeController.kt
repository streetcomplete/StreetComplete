package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList

/** Controller to set/get quest types as enabled or disabled. This controls only the visibility
 *  of quest types per user preference and does not take anything else into account that may
 *  make a quest type invisible (overlays, ...) */
class VisibleQuestTypeController(
    private val visibleQuestTypeDao: VisibleQuestTypeDao,
    private val questPresetsSource: QuestPresetsSource
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
                visibleQuestTypeDao.clear(presetId)
            }
        })
    }

    /** in-memory cache of visible quests */
    private var _visibleQuests: MutableMap<String, Boolean>? = null
    private val visibleQuests: MutableMap<String, Boolean>
        get() = synchronized(this) {
            if (_visibleQuests == null) {
                _visibleQuests = visibleQuestTypeDao.getAll(questPresetsSource.selectedId)
            }
            return _visibleQuests!!
        }

    fun setVisible(questType: QuestType, visible: Boolean) {
        synchronized(this) {
            visibleQuestTypeDao.put(questPresetsSource.selectedId, questType.name, visible)
            visibleQuests[questType.name] = visible
        }
        onQuestTypeVisibilityChanged(questType, visible)
    }

    fun setAllVisible(questTypes: List<QuestType>, visible: Boolean) {
        val questTypeNames = questTypes.filter { it !is OsmNoteQuestType }.map { it.name }
        synchronized(this) {
            visibleQuestTypeDao.put(questPresetsSource.selectedId, questTypeNames, visible)
            questTypeNames.forEach { visibleQuests[it] = visible }
        }
        onQuestTypeVisibilitiesChanged()
    }

    fun clear() {
        synchronized(this) {
            visibleQuestTypeDao.clear(questPresetsSource.selectedId)
            visibleQuests.clear()
        }
        onQuestTypeVisibilitiesChanged()
    }

    override fun isVisible(questType: QuestType): Boolean =
        visibleQuests[questType.name] ?: (questType.defaultDisabledMessage <= 0)

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
