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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.image.loadImageBitmap
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

/** Quest form for the OsmNoteQuestType quest */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddNoteCommentForm(
    onDismiss: () -> Unit,
    onCommentNote: (noteText: String, noteImagePaths: List<String>) -> Unit,
    onHideQuest: () -> Unit,
    quest: Quest,
    note: Note,
    modifier: Modifier = Modifier,
    fileSystem: FileSystem = koinInject(),
    avatarsCacheDir: Path = koinInject(named("AvatarsCacheDirectory")),
    questsHiddenSource: QuestsHiddenSource = koinInject(),
) {
    var noteText by rememberSaveable { mutableStateOf("") }
    var noteImagePaths by rememberSaveable { mutableStateOf(listOf<String>()) }

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

    val alreadyHidden = remember(note.id) {
        questsHiddenSource.get(OsmNoteQuestKey(note.id)) != null
    }

    BottomSheetFormScaffold(
        header = {
            Text(
                text = stringResource(quest.type.title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        content = {
            Column(Modifier.fillMaxWidth()) {
                ProvideTextStyle(MaterialTheme.typography.body2) {
                    NoteCommentItems(
                        noteComments = note.comments.orEmpty(),
                        avatars = avatars,
                        textLinkStyles = MaterialTheme.typography.defaultTextLinkStyles(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Divider()
                NoteForm(
                    onDismiss = onDismiss,
                    text = noteText,
                    onTextChange = { noteText = it },
                    imagePaths = noteImagePaths,
                    onImagePathsChange = { noteImagePaths = it },
                    trackpoints = null,
                    onDeleteTrackpoints = null,
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
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(Res.string.short_no_answer_on_button))
                        }
                    } else {
                        TextButton(onClick = onHideQuest) {
                            Text(stringResource(Res.string.quest_noteDiscussion_no))
                        }
                    }
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = noteText.isNotBlank(),
                onClick = { onCommentNote(noteText, noteImagePaths) },
            )
        },
        modifier = modifier,
    )
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
