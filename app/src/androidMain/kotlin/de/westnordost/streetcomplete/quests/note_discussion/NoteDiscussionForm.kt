package de.westnordost.streetcomplete.quests.note_discussion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionContentBinding
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionItemsBinding
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.RoundRectOutlineProvider
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

        viewLifecycleScope.launch {
            val comments = withContext(Dispatchers.IO) { noteSource.get(noteId) }!!.comments
            inflateNoteDiscussion(comments)
        }
    }

    private fun inflateNoteDiscussion(comments: List<NoteComment>) {
        val discussionView = QuestNoteDiscussionItemsBinding.inflate(layoutInflater, scrollViewChild, false).root

        discussionView.isNestedScrollingEnabled = false
        discussionView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )

        scrollViewChild.addView(discussionView, 0)
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

    private val User.avatar: Bitmap? get() {
        val file = Path(avatarsCacheDir, id.toString())
        return if (fileSystem.exists(file)) BitmapFactory.decodeFile(file.toString()) else null
    }
}
