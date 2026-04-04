package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ZoomableImage
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the image at the given [imagePath] and [fileSystem] is shown, can be zoomed in
 *  and panned, and also deleted. */
@Composable
fun NoteImageDialog(
    onDismissRequest: () -> Unit,
    fileSystem: FileSystem,
    imagePath: String,
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = modifier) {
            val painter = fileBitmapPainter(fileSystem, Path(imagePath))
            if (painter != null) {
                ZoomableImage(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                onDismissRequest()
                            }
                        },
                    initialZoom = 0.85f,
                    zoomRange = 0.85f..4f,
                )
            }
            FloatingActionButton(
                onClick = { showConfirmDeleteDialog = true },
                backgroundColor = Color.Red,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete_24),
                    contentDescription = stringResource(Res.string.quest_leave_new_note_photo_delete),
                )
            }
        }
    }
    if (showConfirmDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            onConfirmed = {
                onDismissRequest()
                onClickDelete()
            },
            title = {
                Text(stringResource(Res.string.quest_leave_new_note_photo_delete_title))
            },
            confirmButtonText = stringResource(Res.string.quest_leave_new_note_photo_delete)
        )
    }
}
