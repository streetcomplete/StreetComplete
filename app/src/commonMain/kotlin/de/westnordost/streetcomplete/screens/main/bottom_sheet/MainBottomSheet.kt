package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
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
import de.westnordost.streetcomplete.ui.common.dialogs.SurveyConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Marker

// TODO appear/disappear animation must be done by

/**
 * Everything that happens in the bottom sheet displayed in the main screen happens here.
 *
 * It actually ought to be displayed at full size, because bottom sheets may have elements that
 * should be displayed above the acutal bottom sheet form (such as a crosshairs, or the arrow when
 * moving a node). So, the actual sliding up/down of the bottom sheet(s) is handled by the forms
 * individually. */
@Composable
fun MainBottomSheet(
    onDismiss: () -> Unit,
    viewModel: MainBottomSheetViewModel,
    shownBottomSheet: ShownBottomSheet,
    geometryOffsetInWindow: Offset?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerPixel: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmEdit by remember { mutableStateOf<PendingEdit?>(null) }

    when (shownBottomSheet) {
        is ShownBottomSheet.CreateOsmNote -> {
            CreateNoteForm(
                onLeaveNote = { noteText, noteImagePaths ->
                    viewModel.createNote(
                        position = mapPosition,
                        text = noteText,
                        imagePaths = noteImagePaths,
                        track = TODO()
                    )
                    onDismiss()
                },
                onDismiss = onDismiss,
                isGpxAttached = shownBottomSheet.isGpxAttached,
                modifier = modifier,
            )
        }
        is ShownBottomSheet.OsmNoteQuest -> {
            AddNoteCommentForm(
                onDismiss = onDismiss,
                onCommentNote = { noteText, noteImagePaths ->
                    viewModel.commentNote(
                        note = shownBottomSheet.note,
                        text = noteText,
                        imagePaths = noteImagePaths
                    )
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmNoteQuestKey(shownBottomSheet.note.id)
                    viewModel.hideQuest(key)
                    onDismiss()
                },
                note = shownBottomSheet.note,
                modifier = modifier,
            )
        }
        is ShownBottomSheet.OsmQuest -> {
            OsmQuestFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    if (SuppressSurveyConfirmation || viewModel.isSurvey(shownBottomSheet.geometry)) {
                        viewModel.submitEdit(
                            elementEditType = shownBottomSheet.questType,
                            geometry = shownBottomSheet.geometry,
                            elementEditAction = action
                        )
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.questType, shownBottomSheet.geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    viewModel.createNote(
                        position = shownBottomSheet.geometry.center,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmQuestKey(
                        shownBottomSheet.element.type,
                        shownBottomSheet.element.id, shownBottomSheet.questType.name)
                    viewModel.hideQuest(key)
                    onDismiss()
                },
                questType = shownBottomSheet.questType,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapPosition = mapPosition,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapMetersPerPixel = mapMetersPerPixel,
                onSetMapMarkers = onSetMapMarkers,
                modifier = modifier,
            )
        }
        is ShownBottomSheet.Overlay -> {
            OverlayFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    val geometry = shownBottomSheet.geometry ?: ElementPointGeometry(mapPosition)

                    if (SuppressSurveyConfirmation || viewModel.isSurvey(geometry)) {
                        viewModel.submitEdit(
                            elementEditType = shownBottomSheet.overlay,
                            geometry = geometry,
                            elementEditAction = action
                        )
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.overlay, geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    viewModel.createNote(
                        position = shownBottomSheet.geometry?.center ?: mapPosition,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                    onDismiss()
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
                modifier = modifier,
            )
        }
    }

    confirmEdit?.let { pendingEdit ->
        SurveyConfirmationDialog(
            onDismissRequest = { confirmEdit = null },
            onConfirmed = {
                viewModel.submitEdit(
                    elementEditType = pendingEdit.elementEditType,
                    geometry = pendingEdit.geometry,
                    elementEditAction = pendingEdit.elementEditAction
                )
                onDismiss()
            },
            onToggleDontShowAgain = { SuppressSurveyConfirmation = it }
        )
    }
}

private data class PendingEdit(
    val elementEditType: ElementEditType,
    val geometry: ElementGeometry,
    val elementEditAction: ElementEditAction,
)

private var SuppressSurveyConfirmation = false
