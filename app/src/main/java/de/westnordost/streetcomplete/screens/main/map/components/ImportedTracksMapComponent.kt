package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/** Shows imported tracks on the map */
class ImportedTracksMapComponent(map: MapLibreMap) :
    DefaultLifecycleObserver {
    private val importedTracksSource = GeoJsonSource("imported-tracks-source")

    private var importedTracks: MutableList<MutableList<LatLon>> = arrayListOf()

    val layers: List<Layer> = listOf(
        LineLayer("imported-tracks", "imported-tracks-source")
            .withProperties(
                lineColor("#147d14"),
                lineWidth(6f),
                linePattern(literal("trackImg")),
                lineOpacity(0.6f),
                lineCap(Property.LINE_CAP_ROUND),
                lineDasharray(arrayOf(0f, 2f))
            ),
    )

    init {
        importedTracksSource.isVolatile = true // TODO [sgr]: check if this is the correct setting
        map.style?.addSource(importedTracksSource)
    }

    @UiThread
    fun clear() {
        importedTracks.clear()
        importedTracksSource.clear()
    }

    private fun updateImportedTracks() {
        val features = importedTracks.map { it.toLineFeature() }
        importedTracksSource.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    @UiThread
    fun replaceImportedTrack(
        pointsList: List<List<LatLon>>,
    ) {
        require(pointsList.isNotEmpty())
        importedTracks = pointsList.map { it.toMutableList() }.toMutableList()
        updateImportedTracks()
    }
}

private fun List<LatLon>.toLineFeature(): Feature {
    val line = LineString.fromLngLats(map { Point.fromLngLat(it.longitude, it.latitude) })
    val p = JsonObject()
    return Feature.fromGeometry(line, p)
}
