package de.westnordost.streetcomplete.quests.note_discussion

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionContentBinding
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.note_comments.NoteCommentItem
import de.westnordost.streetcomplete.util.image.loadImageBitmap
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class NoteDiscussionForm : AbstractQuestForm() {

    override val contentLayoutResId = R.layout.quest_note_discussion_content
    private val binding by contentViewBinding(QuestNoteDiscussionContentBinding::bind)

    override val defaultExpanded = false

    private val noteSource: NotesWithEditsSource by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val hiddenQuestsController: QuestsHiddenController by inject()
    private val fileSystem: FileSystem by inject()
    private val avatarsCacheDir: Path by inject(named("AvatarsCacheDirectory"))

    private val attachPhotoFragment get() =
        childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as? AttachPhotoFragment

    private val noteText: String? get() = binding.noteInput.nonBlankTextOrNull

    private val noteId: Long get() = (questKey as OsmNoteQuestKey).noteId

    private val note: MutableState<Note?> = mutableStateOf(null)
    private val avatars: MutableState<Map<Long, ImageBitmap?>?> = mutableStateOf(null)

    interface Listener {
        /** Called when the user successfully answered the quest */
        fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon)
        /** Called when the user did not answer the quest but also did not hide it */
        fun onNoteQuestClosed()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alreadyHidden = hiddenQuestsController.get(questKey) != null
        setButtonPanelAnswers(listOf(
            if (alreadyHidden) {
                AnswerItem(R.string.short_no_answer_on_button) { closeQuest() }
            } else {
                AnswerItem(R.string.quest_noteDiscussion_no) { hideQuest() }
            }
        ))

        binding.noteInput.doAfterTextChanged { checkIsFormComplete() }

        viewLifecycleScope.launch(Dispatchers.IO) {
            note.value = noteSource.get(noteId)
            avatars.value = note.value?.comments
                ?.mapNotNull { it.user?.id }
                ?.associateWith { fileSystem.loadImageBitmap(Path(avatarsCacheDir, it.toString())) }
        }
    }

    @Composable
    override fun ContentBeforeSpeechbubbleContent() {
        val textLinkStyles = TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colors.primary,
                textDecoration = TextDecoration.Underline
            ),
            focusedStyle = SpanStyle(
                color = MaterialTheme.colors.secondary,
            )
        )
        ProvideTextStyle(MaterialTheme.typography.body2) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp),
            ) {
                for (comment in note.value?.comments.orEmpty()) {
                    NoteCommentItem(
                        noteComment = comment,
                        avatarPainter = comment.user?.id
                            ?.let { avatars.value?.get(it) }
                            ?.let { BitmapPainter(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textLinkStyles = textLinkStyles
                    )
                }
            }
        }
    }

    override fun onClickOk() {
        require(noteText != null) { "NoteQuest has been answered with an empty comment!" }
        val imagePaths = attachPhotoFragment?.imagePaths.orEmpty()
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteEditsController.add(noteId, NoteEditAction.COMMENT, geometry.center, noteText, imagePaths)
            }
            listener?.onNoteQuestSolved(questType, noteId, geometry.center)
        }
    }

    private fun closeQuest() {
        listener?.onNoteQuestClosed()
    }

    private fun hideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { hiddenQuestsController.hide(questKey) }
        }
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose(): Boolean =
        noteText != null || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    override fun isFormComplete(): Boolean = noteText != null
}
