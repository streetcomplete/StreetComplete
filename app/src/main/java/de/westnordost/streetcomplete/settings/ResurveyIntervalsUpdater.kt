package de.westnordost.streetcomplete.settings

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.*
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import javax.inject.Inject
import javax.inject.Singleton

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
@Singleton class ResurveyIntervalsUpdater @Inject constructor(private val prefs: SharedPreferences) {
    fun update() {
        RelativeDate.MULTIPLIER = multiplier
    }

    private val multiplier: Float get() = when(intervalsPreference) {
        LESS_OFTEN -> 2.0f
        DEFAULT -> 1.0f
        MORE_OFTEN -> 0.5f
    }

    private val intervalsPreference: Prefs.ResurveyIntervals get() =
        valueOf(prefs.getString(Prefs.RESURVEY_INTERVALS, "DEFAULT")!!)
}
