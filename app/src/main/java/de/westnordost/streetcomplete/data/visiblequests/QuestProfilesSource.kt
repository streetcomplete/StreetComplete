package de.westnordost.streetcomplete.data.visiblequests

interface QuestProfilesSource {
    interface Listener {
        fun onSelectedQuestProfileChanged()
        fun onAddedQuestProfile(profile: QuestProfile)
        fun onDeletedQuestProfile(profileId: Long)
    }

    val selectedQuestProfileId: Long
    val selectedQuestProfileName: String?

    fun getAllQuestProfiles(): List<QuestProfile>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
