package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_noteDiscussion_no
import de.westnordost.streetcomplete.resources.short_no_answer_on_button
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.util.photo.PhotosViewModel
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import de.westnordost.streetcomplete.util.image.loadImageBitmap
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddNoteCommentForm(
    on: (NoteQuestAction) -> Unit,
    note: Note,
    fileSystem: FileSystem = koinInject(),
    avatarsCacheDir: Path = koinInject(named("AvatarsCacheDirectory")),
    questsHiddenSource: QuestsHiddenSource = koinInject(),
) {
    val viewModel = koinViewModel<PhotosViewModel>()
    val takePhotoSupported = remember { viewModel.isTakePhotoSupported() }
    val noteImagePaths by viewModel.imagePaths.collectAsState()

    var noteText by rememberSaveable { mutableStateOf("") }

    var confirmDiscard by remember { mutableStateOf(false) }

    var avatars by remember { mutableStateOf(mapOf<Long, Painter?>()) }
    LaunchedEffect(note) {
        avatars = note.comments
            .mapNotNull { it.user?.id }
            .associateWith { id ->
                val avatarFile = Path(avatarsCacheDir, id.toString())
                val avatarBitmap = fileSystem.loadImageBitmap(avatarFile)
                avatarBitmap?.let { BitmapPainter(it) }
            }
    }

    val isComplete = noteText.isNotBlank()
    val hasChanges = noteText.isNotBlank() || noteImagePaths.isNotEmpty()

    val alreadyHidden = remember(note.id) {
        questsHiddenSource.get(OsmNoteQuestKey(note.id)) != null
    }

    fun onDiscard() {
        viewModel.deleteAllImagePaths()
        on(NoteQuestAction.Dismiss)
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
            Text(
                text = stringResource(LocalQuestType.current!!.title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        content = {
            ProvideTextStyle(MaterialTheme.typography.body1) {
                Column(Modifier.fillMaxWidth()) {
                    NoteCommentItems(
                        noteComments = note.comments.orEmpty(),
                        avatars = avatars,
                        textLinkStyles = MaterialTheme.typography.defaultTextLinkStyles()
                    )

                    Divider()
                    NoteForm(
                        text = noteText,
                        onTextChange = { noteText = it },
                        addImagesEnabled = takePhotoSupported,
                        onDeleteImage = { viewModel.deleteImagePath(it) },
                        onTakePhoto = { viewModel.takePhoto() },
                        images = noteImagePaths.mapNotNull { fileBitmapPainter(fileSystem, Path(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(horizontal = 24.dp, vertical = 12.dp))
                            .align(Alignment.CenterHorizontally)
                    )

                    Divider()

                    Row(Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                    ) {
                        if (alreadyHidden) {
                            TextButton(onClick = { onDiscard() }) {
                                Text(stringResource(Res.string.short_no_answer_on_button))
                            }
                        } else {
                            TextButton(onClick = { on(NoteQuestAction.HideQuest) }) {
                                Text(stringResource(Res.string.quest_noteDiscussion_no))
                            }
                        }
                    }
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = isComplete,
                onClick = { on(NoteQuestAction.CommentNote(noteText, noteImagePaths)) },
            )
        },
    )

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = true },
            onConfirmed = { onDiscard() },
        )
    }
}

@Composable
private fun NoteCommentItems(
    noteComments: List<NoteComment>,
    avatars: Map<Long, Painter?>,
    textLinkStyles: TextLinkStyles,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        for (noteComment in noteComments.orEmpty()) {
            NoteCommentItem(
                noteComment = noteComment,
                avatarPainter = noteComment.user?.id?.let { avatars.get(it) },
                modifier = Modifier.fillMaxWidth(),
                textLinkStyles = textLinkStyles,
            )
        }
    }
}

sealed interface NoteQuestAction {
    data object Dismiss : NoteQuestAction
    data object HideQuest : NoteQuestAction
    data class CommentNote(val text: String, val attachedImagePaths: List<String>) : NoteQuestAction
}
