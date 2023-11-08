package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.geometry.Polyline
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat
import org.koin.core.component.KoinComponent

/** Shows imported track on the map */
class ImportedTrackMapComponent(ctrl: KtMapController) : KoinComponent {

    private val trackLayer = ctrl.addDataLayer(IMPORTED_TRACK_LAYER)

    fun replaceImportedTrack(
        trackpoints: List<LatLon>,
    ) {
        trackLayer.clear()
        trackLayer.setFeatures(
            listOf(
                Polyline(
                    trackpoints.map { it.toLngLat() }.toMutableList(),
                    mutableMapOf("type" to "line")
                )
            )
        )
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the layer
        private const val IMPORTED_TRACK_LAYER = "streetcomplete_imported_track"
    }
}
