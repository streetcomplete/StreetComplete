package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.ktx.fadingHorizontalScrollEdges
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Row of attached note images with a button to add them and when clicking on one image, a dialog
 *  opens in which one can look at the image more closely and also remove it again. */
@Composable
fun NoteImagesRow(
    fileSystem: FileSystem,
    imagePaths: List<String>,
    onDeleteImage: (imagePath: String) -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Button(onClick = onTakePhoto) {
            Icon(
                painter = painterResource(Res.drawable.ic_add_photo_24),
                contentDescription = stringResource(Res.string.quest_leave_new_note_photo)
            )
        }
        var showNoteImageDialog by remember { mutableStateOf<String?>(null) }

        if (imagePaths.isEmpty()) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.body2,
                LocalContentAlpha provides ContentAlpha.medium
            ) {
                Text(stringResource(Res.string.quest_leave_new_note_photos_are_useful))
            }
        } else {
            val state = rememberLazyListState()
            LazyRow(
                state = state,
                modifier = Modifier.fadingHorizontalScrollEdges(state.scrollIndicatorState, 32.dp)
            ) {
                items(imagePaths) { imagePath ->
                    val painter = fileBitmapPainter(fileSystem, Path(imagePath))
                    if (painter != null) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showNoteImageDialog = imagePath }
                        )
                    }
                }
            }
        }

        showNoteImageDialog?.let { imagePath ->
            NoteImageDialog(
                onDismissRequest = { showNoteImageDialog = null },
                fileSystem = fileSystem,
                imagePath = imagePath,
                onClickDelete = { onDeleteImage(imagePath) },
            )
        }
    }
}
