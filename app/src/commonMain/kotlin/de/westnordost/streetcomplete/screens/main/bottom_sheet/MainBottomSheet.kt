package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.quests.note_comments.AddNoteCommentForm
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.CreateNoteForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay.OverlayFormContainer
import de.westnordost.streetcomplete.screens.main.bottom_sheet.quest.OsmQuestFormContainer
import de.westnordost.streetcomplete.ui.common.quest.Marker

// TODO use modifier!!!
// TODO appear/disappear animation

/** Everything that happens in the bottom sheet displayed in the main screen happens here. */
@Composable
fun MainBottomSheet(
    onDismiss: () -> Unit,
    mainBottomSheetViewModel: MainBottomSheetViewModel,
    shownBottomSheet: ShownBottomSheet,
    geometryOffsetInWindow: Offset?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerPixel: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    modifier: Modifier = Modifier
) {
    when (shownBottomSheet) {
        is ShownBottomSheet.CreateOsmNote -> {
            CreateNoteForm(
                onLeaveNote = { noteText, noteImagePaths ->
                    mainBottomSheetViewModel.createNote(
                        position = mapPosition,
                        text = noteText,
                        imagePaths = noteImagePaths,
                        track = TODO()
                    )
                },
                onDismiss = onDismiss,
                onPinPositioned = { offsetInWindow ->
                    TODO()
                },
                isGpxAttached = shownBottomSheet.isGpxAttached
            )
        }
        is ShownBottomSheet.OsmNoteQuest -> {
            AddNoteCommentForm(
                onDismiss = onDismiss,
                onCommentNote = { noteText, noteImagePaths ->
                    mainBottomSheetViewModel.commentNote(
                        note = shownBottomSheet.note,
                        text = noteText,
                        imagePaths = noteImagePaths
                    )
                },
                onHideQuest = {
                    val key = OsmNoteQuestKey(shownBottomSheet.note.id)
                    mainBottomSheetViewModel.hideQuest(key)
                },
                note = shownBottomSheet.note,
            )
        }
        is ShownBottomSheet.OsmQuest -> {
            OsmQuestFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    mainBottomSheetViewModel.submitEdit(
                        elementEditType = shownBottomSheet.questType,
                        geometry = shownBottomSheet.geometry,
                        elementEditAction = action
                    )
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    mainBottomSheetViewModel.createNote(
                        position = shownBottomSheet.geometry.center,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                },
                onHideQuest = {
                    val key = OsmQuestKey(
                        shownBottomSheet.element.type,
                        shownBottomSheet.element.id, shownBottomSheet.questType.name)
                    mainBottomSheetViewModel.hideQuest(key)
                },
                questType = shownBottomSheet.questType,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapPosition = mapPosition,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapMetersPerPixel = mapMetersPerPixel,
                onSetMapMarkers = onSetMapMarkers
            )
        }
        is ShownBottomSheet.Overlay -> {
            OverlayFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    mainBottomSheetViewModel.submitEdit(
                        elementEditType = shownBottomSheet.overlay,
                        geometry = shownBottomSheet.geometry ?: ElementPointGeometry(mapPosition),
                        elementEditAction = action
                    )
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    mainBottomSheetViewModel.createNote(
                        position = shownBottomSheet.geometry?.center ?: mapPosition,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                },
                overlay = shownBottomSheet.overlay,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapPosition = mapPosition,
                mapMetersPerPixel = mapMetersPerPixel,
                onSetMapMarkers = onSetMapMarkers,
                onSetPinPosition = { icon, position ->
                    TODO()
                },
            )
        }
    }
}
