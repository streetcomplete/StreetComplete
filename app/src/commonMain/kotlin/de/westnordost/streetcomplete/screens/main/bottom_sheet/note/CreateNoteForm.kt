package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.util.photo.PhotosViewModel
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

/** Bottom sheet form with which the user can create a new note, including moving the note */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateNoteForm(
    onLeaveNote: (text: String, noteImagePaths: List<String>) -> Unit,
    onDismiss: () -> Unit,
    onPinPositioned: (offsetInWindow: Offset) -> Unit,
    isGpxAttached: Boolean,
    fileSystem: FileSystem = koinInject(),
) {
    val viewModel = koinViewModel<PhotosViewModel>()
    val takePhotoSupported = remember { viewModel.isTakePhotoSupported() }
    val noteImagePaths by viewModel.imagePaths.collectAsState()

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Pin(
            iconPainter = painterResource(Res.drawable.quest_create_note),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current))
                .onGloballyPositioned { onPinPositioned(it.positionInWindow()) }
                .animateFallDown(startDelay = 200.milliseconds)
        )

        BottomSheetFormScaffold(
            header = {
                QuestHeader(
                    title = stringResource(Res.string.map_btn_create_note),
                    subtitle = null,
                    hintText =
                        stringResource(Res.string.create_new_note_description) +
                            "\n" +
                            stringResource(Res.string.create_new_note_hint),
                    hintImages = emptyList()
                )
            },
            content = {
                ProvideTextStyle(MaterialTheme.typography.body1) {
                    NoteForm(
                        text = noteText,
                        onTextChange = { noteText = it },
                        addImagesEnabled = takePhotoSupported,
                        onDeleteImage = { viewModel.deleteImagePath(it) },
                        onTakePhoto = { viewModel.takePhoto() },
                        images = noteImagePaths.mapNotNull { fileBitmapPainter(fileSystem, Path(it)) },
                        isGpxAttached = isGpxAttached,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(horizontal = 24.dp, vertical = 12.dp))
                    )
                }
            },
            fab = {
                FloatingOkButton(
                    visible = isComplete,
                    onClick = { onLeaveNote(noteText, noteImagePaths) },
                )
            },
        )
    }

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = true },
            onConfirmed = { onDiscard() },
        )
    }
}
