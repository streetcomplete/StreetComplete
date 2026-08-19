package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.toGeoJsonFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.sources.ComputedSource
import org.maplibre.compose.sources.ComputedSourceOptions
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

abstract class MainMapViewModel : ViewModel() {
    abstract val downloadedTiles: StateFlow<Collection<TilePos>>

    abstract val overlaySource: ComputedSource
}

class MainMapViewModelImpl(
    private val downloadedTilesSource: DownloadedTilesSource,
    private val selectedOverlaySource: SelectedOverlaySource,
    private val mapDataWithEditsSource: MapDataWithEditsSource,
) : MainMapViewModel() {
    override val downloadedTiles = MutableStateFlow<Collection<TilePos>>(emptyList())

    override val overlaySource = ComputedSource(
        id = "overlay-source",
        options = ComputedSourceOptions(minZoom = 17, maxZoom = 17, buffer = 0, tolerance = 0f),
        getFeatures = ::getFeatures
    )

    private val selectedOverlay = MutableStateFlow<Overlay?>(null)

    private val downloadedTilesListener = object : DownloadedTilesSource.Listener {
        override fun onUpdated() { updateDownloadedTiles() }
    }
    private val selectedOverlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() { updateSelectedOverlay() }
    }
    private val mapDataWithEditsListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            //TODO("Not yet implemented")
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            overlaySource.invalidateBounds(bbox.toGeoJsonBoundingBox())
        }

        override fun onCleared() {
            //TODO or rather create new source??
            overlaySource.invalidateBounds(GeoJsonBoundingBox(-180.0,-90.0, 180.0, 90.0))
        }
    }

    init {
        updateDownloadedTiles()
        updateSelectedOverlay()
        downloadedTilesSource.addListener(downloadedTilesListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
    }

    override fun onCleared() {
        downloadedTilesSource.removeListener(downloadedTilesListener)
        selectedOverlaySource.removeListener(selectedOverlayListener)
    }

    private fun getFeatures(bounds: GeoJsonBoundingBox, zoomLevel: Int): FeatureCollection<Geometry, JsonObject> {
        val overlay = selectedOverlay.value ?: return FeatureCollection<Geometry, JsonObject>()

        val mapData = mapDataWithEditsSource.getMapDataWithGeometry(bounds.toBoundingBox())
        val styledElements = overlay
            .getStyledElements(mapData)
            .mapNotNull { (element, style) ->
                val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
                StyledElement(element, geometry, style)
            }
            .flatMap { it.toGeoJsonFeatures() }

        return FeatureCollection(styledElements.toList())
    }

    private fun updateSelectedOverlay() {
        selectedOverlay.value = selectedOverlaySource.selectedOverlay
        //TODO or rather create new source??
        overlaySource.invalidateBounds(GeoJsonBoundingBox(-180.0,-90.0, 180.0, 90.0))
    }

    private fun updateDownloadedTiles() {
        launch(Dispatchers.IO) {
            downloadedTiles.value = downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
        }
    }
}
