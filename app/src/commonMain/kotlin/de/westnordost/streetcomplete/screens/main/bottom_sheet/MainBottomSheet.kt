package de.westnordost.streetcomplete.screens.main.bottom_sheet

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
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_create_note
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.CreateNoteForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay.OverlayFormContainer
import de.westnordost.streetcomplete.screens.main.bottom_sheet.quest.OsmQuestFormContainer
import de.westnordost.streetcomplete.ui.common.dialogs.SurveyConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Marker
import org.jetbrains.compose.resources.DrawableResource

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
    onSolved: (icon: DrawableResource, position: LatLon) -> Unit,
    viewModel: MainBottomSheetViewModel,
    shownBottomSheet: ShownBottomSheet,
    geometryOffsetInWindow: Offset?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerDp: Double,
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
                        trackpoints = shownBottomSheet.trackpoints
                    )
                    onSolved(Res.drawable.quest_create_note, mapPosition)
                    onDismiss()
                },
                onDismiss = onDismiss,
                trackpoints = shownBottomSheet.trackpoints,
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
                    onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmNoteQuestKey(shownBottomSheet.note.id)
                    viewModel.hideQuest(key)
                    onDismiss()
                },
                quest = shownBottomSheet.quest,
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
                            elementEditType = shownBottomSheet.quest.type,
                            geometry = shownBottomSheet.geometry,
                            elementEditAction = action
                        )
                        onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.quest.type, shownBottomSheet.geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    viewModel.createNote(
                        position = shownBottomSheet.geometry.center,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                    onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmQuestKey(
                        shownBottomSheet.element.type,
                        shownBottomSheet.element.id, shownBottomSheet.quest.type.name)
                    viewModel.hideQuest(key)
                    onDismiss()
                },
                questType = shownBottomSheet.quest.type,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapPosition = mapPosition,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapMetersPerDp = mapMetersPerDp,
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
                        onSolved(shownBottomSheet.overlay.icon, geometry.center)
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.overlay, geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    val center = shownBottomSheet.geometry?.center ?: mapPosition
                    viewModel.createNote(
                        position = center,
                        text = noteText,
                        imagePaths = noteImagePaths,
                    )
                    onSolved(shownBottomSheet.overlay.icon, center)
                    onDismiss()
                },
                overlay = shownBottomSheet.overlay,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapPosition = mapPosition,
                mapMetersPerDp = mapMetersPerDp,
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
