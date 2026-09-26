package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.ktx.toLocation
import de.westnordost.streetcomplete.util.math.flatDistanceToArcs
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import kotlin.time.Duration.Companion.seconds

/** Checks if user has been (reasonably) close to an element to call it a survey, depending on the
 *  recent locations stored. */
class SurveyChecker(
    private val locationProvider: LocationProvider,
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("SurveyChecker"))

    private val recentLocations = RecentLocations(
        maxAge = ApplicationConstants.MAX_RECENT_LOCATIONS_AGE,
        // we don't need to add elements to `recentLocations` every few meters when we check for a
        // much larger distance (for performance reasons)
        minDistanceMeters = ApplicationConstants.MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY / 2.0,
        // limit the number of new locations added each minute (for performance reasons)
        minTimeDifference = 5.seconds
    )

    init {
        coroutineScope.launch {
            locationProvider.updates().collect { locationEvent ->
                if (locationEvent is LocationEvent.Update) {
                    recentLocations.add(locationEvent.measurement.toLocation())
                }
            }
        }
    }

    /** Returns whether [geometry] is close enough to the previously added recent locations to call
     *  it a survey. */
    fun checkIsSurvey(geometry: ElementGeometry): Boolean {
        val polylines = when (geometry) {
            is ElementPolylinesGeometry -> geometry.polylines
            is ElementPolygonsGeometry -> geometry.polygons
            else -> listOf(listOf(geometry.center))
        }
        return recentLocations.getAll().any { location ->
            polylines.any { polyline ->
                location.position.flatDistanceToArcs(polyline) < location.accuracyMeters + ApplicationConstants.MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY
            }
        }
    }
}
