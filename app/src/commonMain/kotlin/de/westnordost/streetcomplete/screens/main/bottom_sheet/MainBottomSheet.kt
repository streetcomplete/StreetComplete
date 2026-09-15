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
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.quests.note_comments.AddNoteCommentForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_create_note
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.CreateNoteForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.overlay.OverlayFormContainer
import de.westnordost.streetcomplete.screens.main.bottom_sheet.quest.OsmQuestFormContainer
import de.westnordost.streetcomplete.ui.common.dialogs.SurveyConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.MapClick
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
    onHideQuest: (QuestKey) -> Unit,
    isSurvey: (ElementGeometry) -> Boolean,
    onSubmitEdit: (ElementEditType, ElementGeometry, ElementEditAction) -> Unit,
    onCommentNote: (Note, String?, List<String>) -> Unit,
    onCreateNote: (LatLon, String, List<String>, List<Trackpoint>?) -> Unit,
    shownBottomSheet: ShownBottomSheet,
    geometryOffsetInWindow: Offset?,
    mapRotation: Float,
    mapTilt: Float,
    mapPosition: LatLon,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    getOffset: (position: LatLon) -> Offset?,
    lastMapClick: MapClick?,
    modifier: Modifier = Modifier
) {
    var confirmEdit by remember { mutableStateOf<PendingEdit?>(null) }

    when (shownBottomSheet) {
        is ShownBottomSheet.CreateOsmNote -> {
            CreateNoteForm(
                onLeaveNote = { noteText, noteImagePaths, trackpoints ->
                    onCreateNote(
                        mapPosition,
                        noteText,
                        noteImagePaths,
                        trackpoints
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
                    onCommentNote(
                        shownBottomSheet.note,
                        noteText,
                        noteImagePaths
                    )
                    onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmNoteQuestKey(shownBottomSheet.note.id)
                    onHideQuest(key)
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
                    if (SuppressSurveyConfirmation || isSurvey(shownBottomSheet.quest.geometry)) {
                        onSubmitEdit(
                            shownBottomSheet.quest.type,
                            shownBottomSheet.quest.geometry,
                            action
                        )
                        onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.quest.type, shownBottomSheet.quest.geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    onCreateNote(
                        shownBottomSheet.quest.geometry.center,
                        noteText,
                        noteImagePaths,
                        null,
                    )
                    onSolved(shownBottomSheet.quest.type.icon, shownBottomSheet.quest.position)
                    onDismiss()
                },
                onHideQuest = {
                    val key = OsmQuestKey(
                        shownBottomSheet.element.type,
                        shownBottomSheet.element.id, shownBottomSheet.quest.type.name)
                    onHideQuest(key)
                    onDismiss()
                },
                questType = shownBottomSheet.quest.type,
                element = shownBottomSheet.element,
                geometry = shownBottomSheet.quest.geometry,
                geometryOffsetInWindow = geometryOffsetInWindow,
                mapPosition = mapPosition,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapMetersPerDp = mapMetersPerDp,
                onSetMapMarkers = onSetMapMarkers,
                getOffset = getOffset,
                lastMapClick = lastMapClick,
                modifier = modifier,
            )
        }
        is ShownBottomSheet.Overlay -> {
            OverlayFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    val geometry = shownBottomSheet.geometry ?: ElementPointGeometry(mapPosition)

                    if (SuppressSurveyConfirmation || isSurvey(geometry)) {
                        onSubmitEdit(
                            shownBottomSheet.overlay,
                            geometry,
                            action
                        )
                        onSolved(shownBottomSheet.overlay.icon, geometry.center)
                        onDismiss()
                    } else {
                        confirmEdit = PendingEdit(shownBottomSheet.overlay, geometry, action)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    val center = shownBottomSheet.geometry?.center ?: mapPosition
                    onCreateNote(
                        center,
                        noteText,
                        noteImagePaths,
                        null,
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
                getOffset = getOffset,
                lastMapClick = lastMapClick,
                modifier = modifier,
            )
        }
    }

    confirmEdit?.let { pendingEdit ->
        SurveyConfirmationDialog(
            onDismissRequest = { confirmEdit = null },
            onConfirmed = {
                onSubmitEdit(
                    pendingEdit.elementEditType,
                    pendingEdit.geometry,
                    pendingEdit.elementEditAction
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
