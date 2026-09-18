package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.screens.main.MainBottomSheetSelection
import de.westnordost.streetcomplete.screens.main.MainLocationState
import de.westnordost.streetcomplete.screens.main.MainSheetState
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/** Moves the camera to what the user inspects: the object of the open bottom sheet or the edit
 *  selected in the edit history sidebar. Restores the camera when the inspection ends.
 *
 *  Inspection has two steps: the camera mode is entered immediately so that location updates stop
 *  moving the camera and the map is composed with the sheet's padding. The camera then moves to
 *  the inspected object once it has loaded. */
@Composable
internal fun CameraInspectionEffect(
    cameraState: MainMapCameraState,
    sheet: MainSheetState,
    location: MainLocationState,
    tracks: MainMapTrackState,
) {
    LaunchedEffect(cameraState, sheet.selection, sheet.id) {
        when (val selection = sheet.selection) {
            null -> cameraState.closeInspection()
            is MainBottomSheetSelection.EditHistory -> cameraState.openEditHistory(selection.editKey)
            else -> {
                // Inspecting an existing overlay element must leave the map in place.
                val existingOverlay = selection is MainBottomSheetSelection.Overlay && selection.elementKey != null
                cameraState.openSheet(sheet.id, padded = !existingOverlay)
            }
        }
    }
    // Runs once per inspected object.
    val inspection = cameraState.inspection
    LaunchedEffect(cameraState, inspection?.inspectionKey) {
        when (inspection) {
            is CameraMode.Sheet -> {
                if (inspection.id != sheet.id) return@LaunchedEffect
                val shown = snapshotFlow { sheet.shownBottomSheet }.filterNotNull().first()
                when (val selection = sheet.selection) {
                    is MainBottomSheetSelection.CreateNote -> cameraState.composeNote(inspection.id, selection.position)
                    else -> when (shown) {
                        is ShownBottomSheet.OsmQuest -> cameraState.focusSheet(inspection.id, shown.quest.geometry)
                        is ShownBottomSheet.OsmNoteQuest -> cameraState.focusSheet(inspection.id, shown.quest.geometry)
                        else -> cameraState.inspectSheet(inspection.id)
                    }
                }
            }
            is CameraMode.EditHistory -> {
                val shown = snapshotFlow { sheet.shownBottomSheet as? ShownBottomSheet.EditHistory }
                    .filterNotNull().first { it.edit.key == inspection.key }
                cameraState.focusEdit(inspection.key, shown.geometry)
            }
            is CameraMode.Restoring -> cameraState.restore(location.position, getTrackBearing(tracks.currentTrack))
            else -> Unit
        }
    }
}
