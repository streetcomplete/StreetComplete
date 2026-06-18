package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface StatisticsController : StatisticsSource {
    /** Add one edit of the given type */
    fun addOne(type: String, position: LatLon)

    /** Subtract one edit of the given type */
    fun subtractOne(type: String, position: LatLon)

    /** Update all statistics from the given [statistics] object */
    fun updateAll(statistics: Statistics)
}
