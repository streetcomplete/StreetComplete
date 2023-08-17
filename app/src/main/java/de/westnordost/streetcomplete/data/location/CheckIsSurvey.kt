package de.westnordost.streetcomplete.data.location

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.QuestSourceDialogLayoutBinding
import de.westnordost.streetcomplete.util.math.flatDistanceToArcs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Checks if geometry was looked at on a survey, either by looking at the GPS position or asking
 *  the user  */
suspend fun checkIsSurvey(
    context: Context,
    geometry: ElementGeometry,
    locations: Sequence<Location>
): Boolean {
    if (dontShowAgain || isWithinSurveyDistance(geometry, locations)) {
        return true
    }
    return suspendCancellableCoroutine { cont ->
        val dialogBinding = QuestSourceDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialogBinding.checkBoxDontShowAgain.isGone = timesShown < 1

        AlertDialog.Builder(context)
            .setTitle(R.string.quest_source_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                ++timesShown
                dontShowAgain = dialogBinding.checkBoxDontShowAgain.isChecked
                if (cont.isActive) cont.resume(true)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                if (cont.isActive) cont.resume(false)
            }
            .setOnCancelListener {
                if (cont.isActive) cont.resume(false)
            }
            .show()
    }
}

private suspend fun isWithinSurveyDistance(
    geometry: ElementGeometry,
    locations: Sequence<Location>
): Boolean = withContext(Dispatchers.Default) {
    // suspending because distanceToArcs is slow
    val polylines: List<List<LatLon>> = when (geometry) {
        is ElementPolylinesGeometry -> geometry.polylines
        is ElementPolygonsGeometry -> geometry.polygons
        else -> listOf(listOf(geometry.center))
    }
    locations.any { location ->
        val pos = LatLon(location.latitude, location.longitude)
        polylines.any { polyline ->
            pos.flatDistanceToArcs(polyline) < location.accuracy + MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY
        }
    }
}

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

const val MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80f // m

// "static" values, i.e. persisted per application start
private var dontShowAgain = false
private var timesShown = 0
