package de.westnordost.streetcomplete.map

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.distanceToArcs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

/** Checks if the quest was solved on a survey, either by looking at the GPS position or asking
 *  the user  */
class QuestSourceIsSurveyChecker @Inject constructor() {

    suspend fun checkIsSurvey(
        context: Context, geometry: ElementGeometry, locations: List<Location>
    ): Boolean {
        if (dontShowAgain || isWithinSurveyDistance(geometry, locations)) {
            return true
        }
        return suspendCancellableCoroutine { cont ->
            val inner = LayoutInflater.from(context).inflate(R.layout.quest_source_dialog_layout, null, false)
            val checkBox = inner.findViewById<CheckBox>(R.id.checkBoxDontShowAgain)
            checkBox.isGone = timesShown < 1

            AlertDialog.Builder(context)
                .setTitle(R.string.quest_source_dialog_title)
                .setView(inner)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    ++timesShown
                    dontShowAgain = checkBox.isChecked
                    cont.resume(true)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> cont.resume(false) }
                .setOnCancelListener { cont.resume(false) }
                .show()
        }
    }

    private suspend fun isWithinSurveyDistance(
        geometry: ElementGeometry,
        locations: List<Location>
    ): Boolean = withContext(Dispatchers.Default) {
        // suspending because distanceToArcs is slow
        locations.any { location ->
            val pos = LatLon(location.latitude, location.longitude)
            val polylines: List<List<LatLon>> = when (geometry) {
                is ElementPolylinesGeometry -> geometry.polylines
                is ElementPolygonsGeometry -> geometry.polygons
                else -> listOf(listOf(geometry.center))
            }
            polylines.any { polyline ->
                pos.distanceToArcs(polyline) < location.accuracy + MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY
            }
        }
    }

    companion object {
        /*
        Considerations for choosing these values:

        - users should be encouraged to *really* go right there and check even if they think they
          see it from afar already

        - just having walked by something should though still count as survey though. (It might be
          inappropriate or awkward to stop and flip out the smartphone directly there)

        - GPS position might not be updated right after they fetched it out of their pocket, but GPS
          position should be reset to "unknown" (instead of "wrong") when switching back to the app

        - the distance is the minimum distance between the quest geometry (i.e. a road) and the line
          between the user's position when he opened the quest form and the position when he pressed
          "ok", MINUS the current GPS accuracy, so it is a pretty forgiving calculation already
        */

        private const val MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80f //m

        // "static" values persisted per application start
        private var dontShowAgain = false
        private var timesShown = 0
    }
}
