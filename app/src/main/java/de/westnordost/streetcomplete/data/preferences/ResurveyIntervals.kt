package de.westnordost.streetcomplete.data.preferences

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareTagAge
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.osm.toCheckDate

enum class ResurveyIntervals(val multiplier: Float) {
    EVEN_LESS_OFTEN(2.0f),
    LESS_OFTEN(2.0f),
    DEFAULT(1.0f),
    MORE_OFTEN(0.5f)
}

/** This class is just to access the user's preference about which multiplier for the resurvey
 *  intervals to use */
class ResurveyIntervalsUpdater(private val prefs: Preferences) {
    // must hold a local reference to the listener because it is a weak reference
    private val listener: SettingsListener

    fun update() {
        RelativeDate.MULTIPLIER = prefs.resurveyIntervals.multiplier
        CompareTagAge.resurveyKeys = prefs.getString(Prefs.RESURVEY_KEYS, "").split(",").map { it.trim() }
        CompareTagAge.resurveyDate = prefs.getString(Prefs.RESURVEY_DATE, "").toCheckDate()
    }

    init {
        listener = prefs.onResurveyIntervalsChanged { RelativeDate.MULTIPLIER = it.multiplier }
    }
}
