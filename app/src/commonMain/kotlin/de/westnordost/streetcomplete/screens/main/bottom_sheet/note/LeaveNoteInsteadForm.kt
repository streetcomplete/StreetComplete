package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.quests.note_comments.NoteQuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.util.photo.PhotosViewModel
import de.westnordost.streetcomplete.ui.util.photo.compressPhotoAndOverwrite
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Form with which the user can leave a note instead of solving the quest/edit */
@OptIn(ExperimentalComposeUiApi::class)
@Composable fun LeaveNoteInsteadForm(
    onLeaveNote: (text: String, noteImagePaths: List<String>) -> Unit,
    onDismiss: () -> Unit,
    editType: ElementEditType,
    element: Element,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
    fileSystem: FileSystem = koinInject(),
) {
    val viewModel = koinViewModel<PhotosViewModel>()
    val takePhotoSupported = remember { viewModel.isTakePhotoSupported() }
    val noteImagePaths by viewModel.imagePaths.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val takePhotoLauncher = rememberCameraPickerLauncher() { file ->
        if (file != null) {
            coroutineScope.launch {
                FileKit.compressPhotoAndOverwrite(file)
                viewModel.addImagePath(file.path)
            }
        }
    }

    var noteText by rememberSaveable { mutableStateOf("") }

    var confirmDiscard by remember { mutableStateOf(false) }

    val isComplete = noteText.isNotBlank()
    val hasChanges = noteText.isNotBlank() || noteImagePaths.isNotEmpty()

    fun onDiscard() {
        viewModel.deleteAllImagePaths()
        onDismiss()
    }

    BackHandler {
        if (hasChanges) {
            confirmDiscard = true
        } else {
            onDiscard()
        }
    }

    BottomSheetFormScaffold(
        header = {
            QuestHeader(
                title = stringResource(Res.string.map_btn_create_note),
                subtitle = nameAndLocationLabel(element, featureDictionary),
                hintText = stringResource(Res.string.create_new_note_hint),
                hintImages = emptyList()
            )
        },
        content = {
            ProvideTextStyle(MaterialTheme.typography.body1) {
                Column {
                    val resource = when (editType) {
                        is Overlay -> Res.string.leave_note_overlay_context_hint
                        is OsmElementQuestType<*> -> Res.string.leave_note_quest_context_hint
                        else -> null
                    }

                    if (resource != null) {
                        Text(
                            text = stringResource(resource, editType.title),
                            style = MaterialTheme.typography.caption,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        )
                    }

                    NoteForm(
                        text = noteText,
                        onTextChange = { noteText = it },
                        addImagesEnabled = takePhotoSupported,
                        onDeleteImage = { viewModel.deleteImagePath(it) },
                        onTakePhoto = { takePhotoLauncher.launch() },
                        images = noteImagePaths.mapNotNull { fileBitmapPainter(fileSystem, Path(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(horizontal = 24.dp, vertical = 12.dp))
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = isComplete,
                onClick = { onLeaveNote(noteText, noteImagePaths) },
            )
        },
        modifier = modifier,
    )

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = true },
            onConfirmed = { onDiscard() },
        )
    }
}
