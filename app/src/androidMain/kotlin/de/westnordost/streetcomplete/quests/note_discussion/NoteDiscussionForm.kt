package de.westnordost.streetcomplete.quests.note_discussion

import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.note_comments.NoteCommentItem
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.AbstractCreateNoteFragment
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.image.loadImageBitmap
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.photo.TakePhotoFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File

class NoteDiscussionForm : AbstractQuestForm(), TakePhotoFragment.Listener {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    private val noteSource: NotesWithEditsSource by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val hiddenQuestsController: QuestsHiddenController by inject()
    private val fileSystem: FileSystem by inject()
    private val avatarsCacheDir: Path by inject(named("AvatarsCacheDirectory"))

    private val noteId: Long get() = (questKey as OsmNoteQuestKey).noteId

    private val note: MutableState<Note?> = mutableStateOf(null)
    private val avatars: MutableState<Map<Long, ImageBitmap?>?> = mutableStateOf(null)

    private var noteText: MutableState<String> = mutableStateOf("")
    private var noteImagePaths: MutableState<List<String>> = mutableStateOf(emptyList())

    interface Listener {
        /** Called when the user successfully answered the quest */
        fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon)
        /** Called when the user did not answer the quest but also did not hide it */
        fun onNoteQuestClosed()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (childFragmentManager.findFragmentByTag(TAG_TAKE_PHOTO) == null) {
            childFragmentManager.commit { add(TakePhotoFragment(), TAG_TAKE_PHOTO) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addImagesEnabled = requireContext().packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)

        val alreadyHidden = hiddenQuestsController.get(questKey) != null
        setButtonPanelAnswers(listOf(
            if (alreadyHidden) {
                AnswerItem(R.string.short_no_answer_on_button) { closeQuest() }
            } else {
                AnswerItem(R.string.quest_noteDiscussion_no) { hideQuest() }
            }
        ))

        viewLifecycleScope.launch(Dispatchers.IO) {
            note.value = noteSource.get(noteId)
            avatars.value = note.value?.comments
                ?.mapNotNull { it.user?.id }
                ?.associateWith { fileSystem.loadImageBitmap(Path(avatarsCacheDir, it.toString())) }
        }

        binding.composeViewBase.content { Surface {
            noteText = rememberSaveable { mutableStateOf("") }
            noteImagePaths = rememberSerializable { mutableStateOf(emptyList()) }

            NoteForm(
                text = noteText.value,
                onTextChange = {
                    noteText.value = it
                    checkIsFormComplete()
                },
                addImagesEnabled = addImagesEnabled,
                onDeleteImage = ::deleteImage,
                onTakePhoto = { takePhoto() },
                fileSystem = fileSystem,
                imagePaths = noteImagePaths.value
            )
        } }
    }

    @Composable
    override fun ContentBeforeSpeechBubbleContent() {
        val textLinkStyles = MaterialTheme.typography.defaultTextLinkStyles()
        ProvideTextStyle(MaterialTheme.typography.body2) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
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

    private fun takePhoto() {
        (childFragmentManager.findFragmentByTag(TAG_TAKE_PHOTO) as? TakePhotoFragment)?.takePhoto()
    }

    override fun onTookPhoto(path: String) {
        noteImagePaths.value += path
    }

    private fun deleteImage(path: String) {
        fileSystem.delete(Path(path), mustExist = false)
        noteImagePaths.value = noteImagePaths.value.filter { it != path }
    }

    override fun onClickOk() {
        require(noteText.value.isNotBlank()) { "NoteQuest has been answered with an empty comment!" }
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteEditsController.add(noteId, NoteEditAction.COMMENT, geometry.center, noteText.value, noteImagePaths.value)
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
        for (path in noteImagePaths.value) {
            fileSystem.delete(Path(path), mustExist = false)
        }
    }

    override fun isRejectingClose(): Boolean =
        noteText.value.isNotBlank() || noteImagePaths.value.isNotEmpty()

    override fun isFormComplete(): Boolean = noteText.value.isNotBlank()

    companion object {
        private const val TAG_TAKE_PHOTO = "TakePhotoFragment"
    }
}
