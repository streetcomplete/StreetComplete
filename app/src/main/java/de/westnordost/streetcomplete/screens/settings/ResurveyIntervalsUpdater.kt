package de.westnordost.streetcomplete.screens.settings

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.DEFAULT
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.LESS_OFTEN
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.MORE_OFTEN
import de.westnordost.streetcomplete.Prefs.ResurveyIntervals.valueOf
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import com.russhwolf.settings.ObservableSettings

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
class ResurveyIntervalsUpdater(private val prefs: ObservableSettings) {
    fun update() {
        RelativeDate.MULTIPLIER = multiplier
    }

    private val multiplier: Float get() = when (intervalsPreference) {
        LESS_OFTEN -> 2.0f
        DEFAULT -> 1.0f
        MORE_OFTEN -> 0.5f
    }

    private val intervalsPreference: Prefs.ResurveyIntervals get() =
        valueOf(prefs.getString(
            Prefs.RESURVEY_INTERVALS,
            ApplicationConstants.DEFAULT_RESURVEY_INTERVALS
        ))
}
