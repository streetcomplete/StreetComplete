package de.westnordost.streetcomplete.data.messages

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
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
    private val questSelectionHintController: QuestSelectionHintController,
    private val prefs: ObservableSettings
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table*/

    interface UpdateListener {
        fun onNumberOfMessagesUpdated(messageCount: Int)
    }
    private val listeners = Listeners<UpdateListener>()

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
        questSelectionHintController.addListener(object : QuestSelectionHintController.Listener {
            override fun onQuestSelectionHintStateChanged() {
                onNumberOfMessagesUpdated()
            }
        })
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    fun getNumberOfMessages(): Int {
        val shouldShowQuestSelectionHint = questSelectionHintController.state == QuestSelectionHintState.SHOULD_SHOW
        val hasUnreadMessages = userDataController.unreadMessagesCount > 0
        val lastVersion = prefs.getStringOrNull(Prefs.LAST_VERSION)
        val hasNewVersion = lastVersion != null && BuildConfig.VERSION_NAME != lastVersion
        if (lastVersion == null) {
            prefs.putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME)
        }

        var messages = 0
        if (shouldShowQuestSelectionHint) messages++
        if (hasUnreadMessages) messages++
        if (hasNewVersion) messages++
        messages += newAchievements.size
        return messages
    }

    fun popNextMessage(): Message? {
        val lastVersion = prefs.getStringOrNull(Prefs.LAST_VERSION)
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME)
            if (lastVersion != null) {
                onNumberOfMessagesUpdated()
                return NewVersionMessage("v$lastVersion")
            }
        }

        val shouldShowQuestSelectionHint = questSelectionHintController.state == QuestSelectionHintState.SHOULD_SHOW
        if (shouldShowQuestSelectionHint) {
            questSelectionHintController.state = QuestSelectionHintState.SHOWN
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
