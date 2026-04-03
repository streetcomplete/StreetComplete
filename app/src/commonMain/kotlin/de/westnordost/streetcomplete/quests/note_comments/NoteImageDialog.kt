package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.controls.MapButton
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

@Composable
fun NoteImageDialog(
    onDismissRequest: () -> Unit,
    fileSystem: FileSystem,
    imagePath: String,
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(0.85f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val center = size.center.toOffset()
                    val newScale = (scale * zoom).coerceIn(0.85f, 4.0f)
                    val newZoom = newScale / scale
                    val newOffset = (offset + pan) * newZoom + ((centroid - center) * (1f - newZoom))
                    scale = newScale
                    val minX = center.x * (newScale - 1f)
                    val minY = center.y * (newScale - 1f)
                    offset = Offset(
                        newOffset.x.coerceIn(min(-minX, minX), max(-minX, minX)),
                        newOffset.y.coerceIn(min(-minY, minY), max(-minY, minY))
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onDismissRequest()
                }
            }
        ) {
            val painter = fileBitmapPainter(fileSystem, Path(imagePath))
            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
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
