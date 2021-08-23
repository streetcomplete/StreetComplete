package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.DayNightCycle.*
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.DaylightTimes
import de.westnordost.streetcomplete.util.getDaylightTimes
import de.westnordost.streetcomplete.util.isDaylight
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

@Singleton class DayNightQuestFilter @Inject internal constructor() {
    /* This is a singleton because it owns an in-memory cache, no need to duplicate that */

    private val cache: MutableMap<LatLon, DaylightTimes> = mutableMapOf()

    private fun isDaylightAt(pos: LatLon): Boolean {
        // using low precision lat lons (~ city level) because the sun and the earth is big
        val lowPrecisionLatLon = LatLon(round(pos.latitude*10)/10, round(pos.longitude*10)/10)
        val daylightTimes = cache.getOrPut(lowPrecisionLatLon) { getDaylightTimes(pos) }
        return isDaylight(daylightTimes)
    }

    fun isVisible(quest: Quest): Boolean {
        return when (quest.type.dayNightVisibility) {
            DAY_AND_NIGHT -> true
            ONLY_DAY -> isDaylightAt(quest.position)
            ONLY_NIGHT -> !isDaylightAt(quest.position)
        }
    }
}
