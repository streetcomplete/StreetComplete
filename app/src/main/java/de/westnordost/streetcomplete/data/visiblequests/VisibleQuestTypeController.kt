package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class VisibleQuestTypeController @Inject constructor(
    private val visibleQuestTypeDao: VisibleQuestTypeDao,
    private val questProfilesSource: QuestProfilesSource
): VisibleQuestTypeSource {

    private val listeners = CopyOnWriteArrayList<VisibleQuestTypeSource.Listener>()

    init {
        questProfilesSource.addListener(object : QuestProfilesSource.Listener {
            override fun onSelectedQuestProfileChanged() {
                _visibleQuests = null
                onQuestTypeVisibilitiesChanged()
            }
            override fun onAddedQuestProfile(profile: QuestProfile) {}
            override fun onDeletedQuestProfile(profileId: Long) {
                visibleQuestTypeDao.clear(profileId)
            }
        })
    }

    /** in-memory cache of visible quests */
    private var _visibleQuests: MutableMap<String, Boolean>? = null
    private val visibleQuests: MutableMap<String, Boolean>
        get() {
            if (_visibleQuests == null) {
                synchronized(this) {
                    if (_visibleQuests == null) {
                        _visibleQuests = visibleQuestTypeDao.getAll(questProfilesSource.selectedQuestProfileId)
                    }
                }
            }
            return _visibleQuests!!
        }

    fun setVisible(questType: QuestType<*>, visible: Boolean) {
        synchronized(this) {
            visibleQuestTypeDao.put(questProfilesSource.selectedQuestProfileId, questType.name, visible)
            visibleQuests[questType.name] = visible
        }
        onQuestTypeVisibilityChanged(questType, visible)
    }

    fun setAllVisible(questTypes: List<QuestType<*>>, visible: Boolean) {
        val questTypeNames = questTypes.filter { it !is OsmNoteQuestType }.map { it.name }
        synchronized(this) {
            visibleQuestTypeDao.put(questProfilesSource.selectedQuestProfileId, questTypeNames, visible)
            questTypeNames.forEach { visibleQuests[it] = visible }
        }
        onQuestTypeVisibilitiesChanged()
    }

    fun clear() {
        synchronized(this) {
            visibleQuestTypeDao.clear(questProfilesSource.selectedQuestProfileId)
            visibleQuests.clear()
        }
        onQuestTypeVisibilitiesChanged()
    }

    override fun isVisible(questType: QuestType<*>): Boolean =
        visibleQuests[questType.name] ?: (questType.defaultDisabledMessage <= 0)

    override fun addListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.remove(listener)
    }
    private fun onQuestTypeVisibilityChanged(questType: QuestType<*>, visible: Boolean) {
        listeners.forEach { it.onQuestTypeVisibilityChanged(questType, visible) }
    }
    private fun onQuestTypeVisibilitiesChanged() {
        listeners.forEach { it.onQuestTypeVisibilitiesChanged() }
    }
}

private val QuestType<*>.name get() = this::class.simpleName!!
