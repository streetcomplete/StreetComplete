package de.westnordost.streetcomplete.data.visiblequests

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class QuestProfilesController @Inject constructor(
    private val questProfilesDao: QuestProfilesDao,
    private val selectedQuestProfileStore: SelectedQuestProfileStore
): QuestProfilesSource {

    private val listeners = CopyOnWriteArrayList<QuestProfilesSource.Listener>()

    override var selectedQuestProfileId: Long
        get() = selectedQuestProfileStore.get()
        set(value) {
            selectedQuestProfileStore.set(value)
            onSelectedQuestProfileChanged()
        }

    override val selectedQuestProfileName: String? get() =
        questProfilesDao.getName(selectedQuestProfileId)

    fun addQuestProfile(profileName: String): Long {
        val profileId = questProfilesDao.add(profileName)
        onAddedQuestProfile(profileId, profileName)
        return profileId
    }

    fun deleteQuestProfile(profileId: Long) {
        if (profileId == selectedQuestProfileId) {
            selectedQuestProfileId = 0
        }
        questProfilesDao.delete(profileId)
        onDeletedQuestProfile(profileId)
    }

    override fun getAllQuestProfiles(): List<QuestProfile> =
        questProfilesDao.getAll()

    /* listeners */

    override fun addListener(listener: QuestProfilesSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestProfilesSource.Listener) {
        listeners.remove(listener)
    }
    private fun onSelectedQuestProfileChanged() {
        listeners.forEach { it.onSelectedQuestProfileChanged() }
    }
    private fun onAddedQuestProfile(profileId: Long, profileName: String) {
        listeners.forEach { it.onAddedQuestProfile(QuestProfile(profileId, profileName)) }
    }
    private fun onDeletedQuestProfile(profileId: Long) {
        listeners.forEach { it.onDeletedQuestProfile(profileId) }
    }
}
