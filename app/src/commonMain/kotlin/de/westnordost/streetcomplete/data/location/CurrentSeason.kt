package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Month
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.number
import org.maplibre.compose.location.LocationProvider
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.maplibre.compose.location.LocationEvent

/**
 * Provides the season in the users hemisphere and timezone.
 * The implementation is split into multiple functions to allow for easier testing.
 * The season is updated every time a new location is added.
 * */
class CurrentSeason() {
    private var month = Clock.System.todayIn(TimeZone.currentSystemDefault()).month

    enum class Season
    {
        SPRING, SUMMER, AUTUMN, WINTER
    }

    fun getSeason(element: Element) : Season
    {
        val position: LatLon = when (element)
        {
            is Node -> element.position
            else -> LatLon(0.0, 0.0)
        }
        val southernHemisphere = isSouthernHemisphere(position)

        return getSeason(southernHemisphere)
    }

    fun getSeason(southernHemisphere: Boolean): Season {
        return when (month) {
            Month.MARCH, Month.APRIL, Month.MAY ->
                if (southernHemisphere) Season.AUTUMN else Season.SPRING
            Month.JUNE, Month.JULY, Month.AUGUST ->
                if (southernHemisphere) Season.WINTER else Season.SUMMER
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER ->
                if (southernHemisphere) Season.SPRING else Season.AUTUMN
            else -> // DECEMBER, JANUARY, FEBRUARY
                if (southernHemisphere) Season.SUMMER else Season.WINTER
        }
    }

    fun isSouthernHemisphere(location: LatLon): Boolean{
        return location.latitude < 0
    }
}
