package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osmcal.OsmCalUpdater
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.weeklyosm.WeeklyOsmUpdater
import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.datetime.LocalDate

/** Updates user data and from feeds, i.e. checks for new weeklyOSM, events, ... */
class FeedsUpdater(
    private val userUpdater: UserUpdater,
    private val weeklyOsmUpdater: WeeklyOsmUpdater,
    private val osmCalUpdater: OsmCalUpdater,
    private val prefs: Preferences,
    private val achievementsSource: AchievementsSource,
) {
    /** update at most daily */
    fun updateAtMostDaily() {
        val today = LocalDate.now()
        val lastUpdate = prefs.lastFeedUpdate
        if (lastUpdate != null && lastUpdate >= today) return

        updateNow()
    }

    /** (force) update now */
    fun updateNow() {
        prefs.lastFeedUpdate = LocalDate.now()

        userUpdater.update()
        val disabledMessageTypes = prefs.disabledMessageTypes
        if (
            Message.NewWeeklyOsm::class !in disabledMessageTypes &&
            achievementsSource.hasLink("weeklyosm")
        ) {
            weeklyOsmUpdater.update()
        }
        if (
            Message.NewCalendarEvent::class !in disabledMessageTypes &&
            achievementsSource.hasLink("calendar")
        ) {
            osmCalUpdater.update()
        }
    }
}
