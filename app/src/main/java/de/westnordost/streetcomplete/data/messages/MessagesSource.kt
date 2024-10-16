package de.westnordost.streetcomplete.data.messages

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants.QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.QuestSelectionHintState
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.user.UserDataController
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.util.Listeners

/** This class is to access user messages, which are basically dialogs that pop up when
 *  clicking on the mail icon, such as "you have a new OSM message in your inbox" etc. */
class MessagesSource(
    private val userDataController: UserDataController,
    private val achievementsSource: AchievementsSource,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: Preferences,
    private val changelog: Changelog,
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table*/

    interface UpdateListener {
        fun onNumberOfMessagesUpdated(messageCount: Int)
    }
    private val listeners = Listeners<UpdateListener>()

    private val settingsListeners = mutableListOf<SettingsListener>()

    /** Achievement levels unlocked since application start. I.e. when restarting the app, the
     *  messages about new achievements unlocked are lost, this is deliberate */
    private val newAchievements = ArrayList<Pair<Achievement, Int>>()

    init {
        userDataController.addListener(object : UserDataSource.Listener {
            override fun onUpdated() {
                onNumberOfMessagesUpdated()
            }
        })
        achievementsSource.addListener(object : AchievementsSource.Listener {
            override fun onAchievementUnlocked(achievement: Achievement, level: Int) {
                newAchievements.add(achievement to level)
                onNumberOfMessagesUpdated()
            }

            override fun onAllAchievementsUpdated() {
                // when all achievements have been updated, this doesn't spawn any messages
            }
        })
        visibleQuestsSource.addListener(object : VisibleQuestsSource.Listener {
            override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
                if (prefs.questSelectionHintState == QuestSelectionHintState.NOT_SHOWN) {
                    if (added.size >= QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT) {
                        prefs.questSelectionHintState = QuestSelectionHintState.SHOULD_SHOW
                    }
                }
            }

            override fun onVisibleQuestsInvalidated() {}
        })

        // must hold a reference because the listener is a weak reference
        settingsListeners += prefs.onQuestSelectionHintStateChanged { onNumberOfMessagesUpdated() }
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    fun getNumberOfMessages(): Int {
        val shouldShowQuestSelectionHint = prefs.questSelectionHintState == QuestSelectionHintState.SHOULD_SHOW
        val hasUnreadMessages = userDataController.unreadMessagesCount > 0
        val lastVersion = prefs.lastChangelogVersion
        val hasNewVersion = lastVersion != null && BuildConfig.VERSION_NAME != lastVersion
        if (lastVersion == null) {
            prefs.lastChangelogVersion = BuildConfig.VERSION_NAME
        }

        var messages = 0
        if (shouldShowQuestSelectionHint) messages++
        if (hasUnreadMessages) messages++
        if (hasNewVersion) messages++
        messages += newAchievements.size
        return messages
    }

    suspend fun popNextMessage(): Message? {
        val lastVersion = prefs.lastChangelogVersion
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.lastChangelogVersion = BuildConfig.VERSION_NAME
            if (lastVersion != null) {
                val version = "v$lastVersion"
                onNumberOfMessagesUpdated()
                return NewVersionMessage(changelog.getChangelog(version))
            }
        }

        val shouldShowQuestSelectionHint = prefs.questSelectionHintState == QuestSelectionHintState.SHOULD_SHOW
        if (shouldShowQuestSelectionHint) {
            prefs.questSelectionHintState = QuestSelectionHintState.SHOWN
            return QuestSelectionHintMessage
        }

        val newAchievement = newAchievements.removeFirstOrNull()
        if (newAchievement != null) {
            onNumberOfMessagesUpdated()
            return NewAchievementMessage(newAchievement.first, newAchievement.second)
        }

        val unreadOsmMessages = userDataController.unreadMessagesCount
        if (unreadOsmMessages > 0) {
            userDataController.unreadMessagesCount = 0
            return OsmUnreadMessagesMessage(unreadOsmMessages)
        }

        return null
    }

    private fun onNumberOfMessagesUpdated() {
        listeners.forEach { it.onNumberOfMessagesUpdated(getNumberOfMessages()) }
    }
}
