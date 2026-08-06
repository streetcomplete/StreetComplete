package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osmnotes.getEditTypeContextForNote
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Form with which the user can leave a note instead of solving the quest/edit */
@OptIn(ExperimentalComposeUiApi::class)
@Composable fun LeaveNoteInsteadForm(
    onLeaveNote: (text: String, noteImagePaths: List<String>) -> Unit,
    onDismiss: () -> Unit,
    editType: ElementEditType,
    element: Element?,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()

    var noteText by rememberSaveable { mutableStateOf("") }
    var noteImagePaths by rememberSaveable { mutableStateOf(listOf<String>()) }

    BottomSheetFormScaffold(
        header = {
            QuestHeader(
                title = stringResource(Res.string.map_btn_create_note),
                subtitle = element?.let { nameAndLocationLabel(element, featureDictionary) },
                hintText = stringResource(Res.string.create_new_note_hint),
                hintImages = emptyList()
            )
        },
        content = {
            ProvideTextStyle(MaterialTheme.typography.body1) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val resource = when (editType) {
                        is Overlay -> Res.string.leave_note_overlay_context_hint
                        is OsmElementQuestType<*> -> Res.string.leave_note_quest_context_hint
                        else -> null
                    }

                    if (resource != null) {
                        val title = stringResource(editType.title)
                        Text(
                            text = stringResource(resource, AnnotatedString(title, SpanStyle(fontStyle = FontStyle.Italic))),
                            style = MaterialTheme.typography.body2,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        )
                    }

                    NoteForm(
                        onDismiss = onDismiss,
                        text = noteText,
                        onTextChange = { noteText = it },
                        imagePaths = noteImagePaths,
                        onImagePathsChange = { noteImagePaths = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = noteText.isNotBlank(),
                onClick = {
                    coroutineScope.launch {
                        val context = getEditTypeContextForNote(element, editType, featureDictionary)
                        onLeaveNote("$context\n\n$noteText", noteImagePaths)
                    }
                },
            )
        },
        modifier = modifier,
    )
}
