package de.westnordost.streetcomplete.data.preferences

import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate

enum class ResurveyIntervals(val multiplier: Float) {
    LESS_OFTEN(2.0f),
    DEFAULT(1.0f),
    MORE_OFTEN(0.5f)
}

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
class ResurveyIntervalsUpdater(private val prefs: Preferences) {
    fun update() {
        RelativeDate.MULTIPLIER = prefs.resurveyIntervals.multiplier
    }

    init {
        prefs.onResurveyIntervalsChanged(::update)
    }
}
