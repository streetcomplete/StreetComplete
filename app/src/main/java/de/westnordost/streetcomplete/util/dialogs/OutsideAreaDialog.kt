package de.westnordost.streetcomplete.util.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun showOutsideDownloadedAreaDialog(context: Context, position: LatLon, downloadedTilesSource: DownloadedTilesSource, onOk: () -> Unit) {
    if (!downloadedTilesSource.contains(position.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM).toTilesRect(), 0L))
        AlertDialog.Builder(context)
            .setTitle(R.string.general_warning)
            .setMessage(R.string.outside_downloaded_area_warning)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> onOk() }
            .setCancelable(false)
            .show()
    else
        onOk()
}
