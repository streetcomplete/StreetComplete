package de.westnordost.streetcomplete.view

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.location.RecentLocationStore.Companion.MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY
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

/** Asks user if he was really on-site */
suspend fun confirmIsSurvey(context: Context): Boolean {
    if (dontShowAgain) return true
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

/** Checks if geometry was looked at on a survey, by looking at the GPS position */
suspend fun checkIsSurvey(
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

// "static" values, i.e. persisted per application start
private var dontShowAgain = false
private var timesShown = 0
