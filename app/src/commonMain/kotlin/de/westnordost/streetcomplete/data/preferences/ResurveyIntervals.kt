package de.westnordost.streetcomplete.data.preferences

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate

enum class ResurveyIntervals(val multiplier: Float) {
    LESS_OFTEN(2.0f),
    DEFAULT(1.0f),
    MORE_OFTEN(0.5f)
}

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
class ResurveyIntervalsUpdater(private val prefs: Preferences) {
    // must hold a local reference to the listener because it is a weak reference
    @Suppress("unused")
    private val listener: SettingsListener =
        prefs.onResurveyIntervalsChanged { RelativeDate.MULTIPLIER = it.multiplier }

    fun update() {
        RelativeDate.MULTIPLIER = prefs.resurveyIntervals.multiplier
    }
}
