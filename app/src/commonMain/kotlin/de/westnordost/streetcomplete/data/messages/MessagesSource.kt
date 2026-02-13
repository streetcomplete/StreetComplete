package de.westnordost.streetcomplete.data.messages

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants.QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.changelog.readChangelog
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.QuestSelectionHintState
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.user.UserDataController
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.util.Listeners

/** This class is to access user messages, which are basically dialogs that pop up when
 *  clicking on the mail icon, such as "you have a new OSM message in your inbox" etc. */
class MessagesSource(
    private val userDataController: UserDataController,
    private val achievementsSource: AchievementsSource,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: Preferences,
    private val res: Res,
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    interface UpdateListener {
        fun onNumberOfMessagesUpdated(messageCount: Int)
    }
    private val listeners = Listeners<UpdateListener>()

    private val settingsListeners = mutableListOf<SettingsListener>()

    /** Achievement levels unlocked since application start. I.e. when restarting the app, the
     *  messages about new achievements unlocked are lost, this is deliberate */
    private val newAchievements = ArrayList<Message.NewAchievement>()

    init {
        userDataController.addListener(object : UserDataSource.Listener {
            override fun onUpdated() {
                onNumberOfMessagesUpdated()
            }
        })
        achievementsSource.addListener(object : AchievementsSource.Listener {
            override fun onAchievementUnlocked(achievement: Achievement, level: Int, unlockedLinks: List<Link>) {
                newAchievements.add(Message.NewAchievement(achievement, level, unlockedLinks))
                onNumberOfMessagesUpdated()
            }

            override fun onAllAchievementsUpdated() {
                // when all achievements have been updated, this doesn't spawn any messages
            }
        })
        visibleQuestsSource.addListener(object : VisibleQuestsSource.Listener {
            override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
                if (prefs.questSelectionHintState == QuestSelectionHintState.NOT_SHOWN) {
                    if (added.size >= QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT) {
                        prefs.questSelectionHintState = QuestSelectionHintState.SHOULD_SHOW
                    }
                }
            }

            override fun onInvalidated() {}
        })

        // must hold a reference because the listener is a weak reference
        settingsListeners += prefs.onDisabledMessageTypesChanged { onNumberOfMessagesUpdated() }
        settingsListeners += prefs.onQuestSelectionHintStateChanged { onNumberOfMessagesUpdated() }
        settingsListeners += prefs.onWeeklyOsmLastPublishDateChanged { onNumberOfMessagesUpdated() }
        settingsListeners += prefs.onWeeklyOsmLastNotifiedPublishDateChanged { onNumberOfMessagesUpdated() }
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    fun getNumberOfMessages(): Int {
        val disabled = prefs.disabledMessageTypes

        val showQuestSelectionHint =
            Message.QuestSelectionHint::class !in disabled &&
            prefs.questSelectionHintState == QuestSelectionHintState.SHOULD_SHOW

        val showUnreadMessages =
            Message.OsmUnreadMessages::class !in disabled &&
            userDataController.unreadMessagesCount > 0

        val showNewWeeklyOsm =
            Message.NewWeeklyOsm::class !in disabled &&
            prefs.weeklyOsmLastPublishDate != null &&
            prefs.weeklyOsmLastPublishDate != prefs.weeklyOsmLastNotifiedPublishDate &&
            achievementsSource.getLinks().any { it.id == "weeklyosm" }

        val showNewAchievements = Message.NewAchievement::class !in disabled

        val lastVersion = prefs.lastChangelogVersion
        // lastVersion is null on a new install. We don't want to show a message in that case, but
        // we want to mark it as if a message has already been read.
        // The same with when the message type is disabled
        if (lastVersion == null || Message.NewVersion::class !in disabled) {
            prefs.lastChangelogVersion = BuildConfig.VERSION_NAME
        }
        val showNewVersion =
            Message.NewVersion::class !in disabled &&
            lastVersion != null && BuildConfig.VERSION_NAME != lastVersion

        var messages = 0
        if (showQuestSelectionHint) messages++
        if (showUnreadMessages) messages++
        if (showNewVersion) messages++
        if (showNewWeeklyOsm) messages++
        if (showNewAchievements) messages += newAchievements.size
        return messages
    }

    suspend fun popNextMessage(): Message? {
        val disabled = prefs.disabledMessageTypes

        if (Message.NewVersion::class !in disabled) {
            val lastVersion = prefs.lastChangelogVersion
            if (BuildConfig.VERSION_NAME != lastVersion) {
                prefs.lastChangelogVersion = BuildConfig.VERSION_NAME
                if (lastVersion != null) {
                    val version = "v$lastVersion"
                    onNumberOfMessagesUpdated()
                    return Message.NewVersion(res.readChangelog(version))
                }
            }
        }

        if (Message.QuestSelectionHint::class !in disabled) {
            val shouldShowQuestSelectionHint = prefs.questSelectionHintState == QuestSelectionHintState.SHOULD_SHOW
            if (shouldShowQuestSelectionHint) {
                prefs.questSelectionHintState = QuestSelectionHintState.SHOWN
                return Message.QuestSelectionHint
            }
        }

        if (Message.OsmUnreadMessages::class !in disabled) {
            val unreadOsmMessages = userDataController.unreadMessagesCount
            if (unreadOsmMessages > 0) {
                userDataController.unreadMessagesCount = 0
                return Message.OsmUnreadMessages(unreadOsmMessages)
            }
        }

        if (Message.NewAchievement::class !in disabled) {
            val newAchievement = newAchievements.removeFirstOrNull()
            if (newAchievement != null) {
                onNumberOfMessagesUpdated()
                return newAchievement
            }
        }

        if (Message.NewWeeklyOsm::class !in disabled) {
            val weeklyOsmPublishDate = prefs.weeklyOsmLastPublishDate
            if (
                weeklyOsmPublishDate != null
                && weeklyOsmPublishDate != prefs.weeklyOsmLastNotifiedPublishDate
                && achievementsSource.getLinks().any { it.id == "weeklyosm" }
            ) {
                prefs.weeklyOsmLastNotifiedPublishDate = weeklyOsmPublishDate
                return Message.NewWeeklyOsm(weeklyOsmPublishDate)
            }
        }

        return null
    }

    private fun onNumberOfMessagesUpdated() {
        listeners.forEach { it.onNumberOfMessagesUpdated(getNumberOfMessages()) }
    }
}
