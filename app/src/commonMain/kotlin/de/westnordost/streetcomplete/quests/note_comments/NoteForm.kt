package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.util.photo.compressPhotoAndOverwrite
import de.westnordost.streetcomplete.ui.util.photo.createOpenCameraSettings
import de.westnordost.streetcomplete.ui.util.photo.createPhotoPlatformFile
import de.westnordost.streetcomplete.ui.util.photo.rememberHasCamera
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Form in which you can leave a note, with images */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteForm(
    onDismiss: () -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    imagePaths: List<String>,
    onImagePathsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    isGpxAttached: Boolean = false,
    fileSystem: FileSystem = koinInject(),
) {
    val hasCamera = rememberHasCamera()
    var path by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val takePhotoLauncher = rememberCameraPickerLauncher(createOpenCameraSettings()) { file ->
        val path2 = path ?: return@rememberCameraPickerLauncher
        coroutineScope.launch(Dispatchers.IO) {
            if (file != null) {
                FileKit.compressPhotoAndOverwrite(PlatformFile(path2))
                onImagePathsChange(imagePaths + path2)
            } else {
                fileSystem.delete(Path(path2), mustExist = false)
                path = null
            }
        }
    }
    var confirmDiscard by remember { mutableStateOf(false) }

    val hasChanges = text.isNotBlank() || imagePaths.isNotEmpty()

    fun onDiscard() {
        coroutineScope.launch(Dispatchers.IO) {
            imagePaths.forEach { fileSystem.delete(Path(it), mustExist = false) }
        }
        onDismiss()
    }

    BackHandler {
        if (hasChanges) {
            confirmDiscard = true
        } else {
            onDiscard()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None
            ),
            singleLine = false,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        if (isGpxAttached) {
            Text(
                text = stringResource(Res.string.quest_leave_new_note_track_recording),
                modifier = Modifier.alpha(ContentAlpha.medium)
            )
        }
        if (hasCamera) {
            NoteImagesRow(
                images = imagePaths.mapNotNull { fileBitmapPainter(fileSystem, Path(it)) },
                onDeleteImage = { index ->
                    val image = imagePaths[index]
                    onImagePathsChange(imagePaths.toMutableList().apply { removeAt(index) })
                    coroutineScope.launch(Dispatchers.IO) { fileSystem.delete(Path(image)) }
                },
                onTakePhoto = {
                    val file = createPhotoPlatformFile()
                    path = file.path
                    takePhotoLauncher.launch(destinationFile = file)
                },
                // because otherwise it would overlap with the OK button
                modifier = Modifier.padding(end = 72.dp)
            )
        }
    }

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = false },
            onConfirmed = { onDiscard() },
        )
    }
}
