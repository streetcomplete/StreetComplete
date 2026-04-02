package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.screens.main.controls.MapButton
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

@Composable
fun NoteImageDialog(
    onDismissRequest: () -> Unit,
    fileSystem: FileSystem,
    imagePath: String,
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO
    // Res.string.quest_leave_new_note_photo_delete_title

    // see NoteImageAdapter

    // minimum scale 0.75
    // maximum scale 4
    // fit center?
    // tap outside: done
    //

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier,
        ) {
            val painter = fileBitmapPainter(fileSystem, Path(imagePath))
            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                // TODO
                            }
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, rotation ->
                                // TODO
                            }
                        }
                )
            }

        }
    }
}
