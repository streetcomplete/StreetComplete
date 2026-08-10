package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.theme.Dimensions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

/** Bottom sheet form with which the user can create a new note, including moving the note */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateNoteForm(
    onLeaveNote: (noteText: String, noteImagePaths: List<String>, trackpoints: List<Trackpoint>?) -> Unit,
    onDismiss: () -> Unit,
    trackpoints: List<Trackpoint>?,
    modifier: Modifier = Modifier,
) {
    var noteText by rememberSaveable { mutableStateOf("") }
    var noteImagePaths by rememberSaveable { mutableStateOf(listOf<String>()) }
    var trackpointsDeleted by rememberSaveable { mutableStateOf(false) }

    val trackpoints = if (trackpointsDeleted) null else trackpoints

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Pin(
            iconPainter = painterResource(Res.drawable.quest_create_note),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current))
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
                        onDismiss = onDismiss,
                        text = noteText,
                        onTextChange = { noteText = it },
                        imagePaths = noteImagePaths,
                        onImagePathsChange = { noteImagePaths = it },
                        trackpoints = trackpoints,
                        onDeleteTrackpoints = { trackpointsDeleted = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(horizontal = 24.dp, vertical = 12.dp))
                    )
                }
            },
            fab = {
                FloatingOkButton(
                    visible = noteText.isNotBlank(),
                    onClick = { onLeaveNote(noteText, noteImagePaths, trackpoints) },
                )
            },
        )
    }
}
