package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlin.math.PI
import kotlin.math.sqrt

internal sealed interface MainMapDownloadArea {
    data class Available(val bounds: BoundingBox) : MainMapDownloadArea
    data object DisplayAreaUnavailable : MainMapDownloadArea
    data object TooLarge : MainMapDownloadArea
}

/** Applies StreetComplete's production tile alignment and download-area limits. */
internal fun calculateMainMapDownloadArea(
    displayArea: BoundingBox?,
    cameraTarget: LatLon,
): MainMapDownloadArea {
    if (displayArea == null) return MainMapDownloadArea.DisplayAreaUnavailable

    val enclosingBounds = displayArea.asBoundingBoxOfEnclosingTiles(
        ApplicationConstants.DOWNLOAD_TILE_ZOOM
    )
    val areaInSquareKilometers = enclosingBounds.area() / 1_000_000
    if (areaInSquareKilometers > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
        return MainMapDownloadArea.TooLarge
    }

    if (areaInSquareKilometers < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
        val radius = sqrt(
            1_000_000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI
        )
        return MainMapDownloadArea.Available(cameraTarget.enclosingBoundingBox(radius))
    }

    return MainMapDownloadArea.Available(enclosingBounds)
}
