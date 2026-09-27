package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.screens.main.MainSheetSelection
import de.westnordost.streetcomplete.screens.main.MainSheetState
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.maplibre.spatialk.geojson.Position

/** Moves the camera to what the user inspects: the object of the open bottom sheet or the edit
 *  selected in the edit history sidebar. Returns the camera when the sheet is closed.
 *
 *  The sheet is opened on the camera before its object has loaded so that location updates stop
 *  moving the camera right away. */
@Composable
internal fun CameraInspectionEffect(
    cameraState: MainMapCameraState,
    sheet: MainSheetState,
    position: Position?,
    tracks: MainMapTrackState,
) {
    LaunchedEffect(cameraState, sheet.selection, sheet.id) {
        when (val selection = sheet.selection) {
            null -> {
                cameraState.closeSheet(position?.toLatLon(), getTrackBearing(tracks.currentTrack))
            }
            is MainSheetSelection.EditHistory -> {
                cameraState.openSheet(padded = false)
                val shown = snapshotFlow { sheet.shownEdit }
                    .filterNotNull().first { it.edit.key == selection.editKey }
                cameraState.focus(shown.geometry)
            }
            is MainSheetSelection.CreateNote -> {
                cameraState.openSheet(padded = true)
                cameraState.focus(selection.position)
            }
            else -> {
                // Inspecting an existing overlay element must leave the map in place.
                val existingOverlay = selection is MainSheetSelection.Overlay && selection.elementKey != null
                cameraState.openSheet(padded = !existingOverlay)
                when (val shown = snapshotFlow { sheet.shownBottomSheet }.filterNotNull().first()) {
                    is ShownBottomSheet.OsmQuest -> cameraState.focus(shown.quest.geometry)
                    is ShownBottomSheet.OsmNoteQuest -> cameraState.focus(shown.quest.geometry)
                    else -> {}
                }
            }
        }
    }
}
