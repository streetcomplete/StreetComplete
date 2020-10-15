package de.westnordost.streetcomplete.settings

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.*
import javax.inject.Inject
import javax.inject.Singleton

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
@Singleton class ResurveyIntervalsStore @Inject constructor(private val prefs: SharedPreferences) {
    operator fun times(num: Double) = num * multiplier
    operator fun times(num: Int) = num * multiplier

    private val multiplier: Double get() = when(intervalsPreference) {
        LESS_OFTEN -> 2.0
        DEFAULT -> 1.0
        MORE_OFTEN -> 0.5
    }

    private val intervalsPreference: Prefs.ResurveyIntervals get() =
        valueOf(prefs.getString(Prefs.RESURVEY_INTERVALS, "DEFAULT")!!)
}
