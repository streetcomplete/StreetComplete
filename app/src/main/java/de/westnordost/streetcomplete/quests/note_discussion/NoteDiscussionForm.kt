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
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionContentBinding
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionItemBinding
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionItemsBinding
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.RoundRectOutlineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File

class NoteDiscussionForm : AbstractQuestForm() {

    override val contentLayoutResId = R.layout.quest_note_discussion_content
    private val binding by contentViewBinding(QuestNoteDiscussionContentBinding::bind)

    override val defaultExpanded = false

    private lateinit var anonAvatar: Bitmap

    private val noteSource: NotesWithEditsSource by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val osmNoteQuestController: OsmNoteQuestController by inject()
    private val avatarsCacheDir: File by inject(named("AvatarsCacheDirectory"))

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

        val alreadyHidden = osmNoteQuestController.getVisible(noteId) == null
        setButtonPanelAnswers(listOf(
            if (alreadyHidden) AnswerItem(R.string.short_no_answer_on_button) { closeQuest() }
            else               AnswerItem(R.string.quest_noteDiscussion_no) { hideQuest() }
        ))

        binding.noteInput.doAfterTextChanged { checkIsFormComplete() }

        anonAvatar = requireContext().getDrawable(R.drawable.ic_osm_anon_avatar)!!.createBitmap()

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
        discussionView.adapter = NoteCommentListAdapter(comments)

        scrollViewChild.addView(discussionView, 0)
    }

    override fun onClickOk() {
        require(noteText != null ) { "NoteQuest has been answered with an empty comment!" }
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
            withContext(Dispatchers.IO) { osmNoteQuestController.hide(noteId) }
        }
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose(): Boolean =
        noteText != null || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    override fun isFormComplete(): Boolean = noteText != null

    private inner class NoteCommentListAdapter(list: List<NoteComment>) : ListAdapter<NoteComment>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<NoteComment> {
            return NoteCommentViewHolder(
                QuestNoteDiscussionItemBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    private inner class NoteCommentViewHolder(private val itemBinding: QuestNoteDiscussionItemBinding) :
        ListAdapter.ViewHolder<NoteComment>(itemBinding) {

        init {
            val cornerRadius = resources.getDimension(R.dimen.speech_bubble_rounded_corner_radius)
            val margin = resources.getDimensionPixelSize(R.dimen.horizontal_speech_bubble_margin)
            val marginStart = -resources.getDimensionPixelSize(R.dimen.quest_form_speech_bubble_top_margin)
            itemBinding.commentStatusText.outlineProvider = RoundRectOutlineProvider(cornerRadius)

            val isRTL = itemView.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            val marginLeft = if (isRTL) 0 else marginStart
            val marginRight = if (isRTL) marginStart else 0
            itemBinding.commentBubble.outlineProvider = RoundRectOutlineProvider(
                cornerRadius, marginLeft, margin, marginRight, margin
            )
            itemBinding.commentAvatarImageContainer.outlineProvider = CircularOutlineProvider
        }

        override fun onBind(with: NoteComment) {
            val dateDescription = DateUtils.getRelativeTimeSpanString(with.timestamp, nowAsEpochMilliseconds(), MINUTE_IN_MILLIS)
            val userName = if (with.user != null) with.user.displayName else getString(R.string.quest_noteDiscussion_anonymous)

            val commentActionResourceId = with.action.actionResourceId
            val hasNoteAction = commentActionResourceId != 0
            itemBinding.commentStatusText.isGone = !hasNoteAction
            if (hasNoteAction) {
                itemBinding.commentStatusText.text = getString(commentActionResourceId, userName, dateDescription)
            }

            val hasComment = with.text?.isNotEmpty() == true
            itemBinding.commentView.isGone = !hasComment
            if (hasComment) {
                itemBinding.commentText.text = with.text
                itemBinding.commentInfoText.text = getString(R.string.quest_noteDiscussion_comment2, userName, dateDescription)

                val bitmap = with.user?.avatar ?: anonAvatar
                itemBinding.commentAvatarImage.setImageBitmap(bitmap)
            }
        }

        private val User.avatar: Bitmap? get() {
            val file = File(avatarsCacheDir.toString() + File.separator + id)
            return if (file.exists()) BitmapFactory.decodeFile(file.path) else null
        }

        private val NoteComment.Action.actionResourceId get() = when (this) {
            NoteComment.Action.CLOSED -> R.string.quest_noteDiscussion_closed2
            NoteComment.Action.REOPENED -> R.string.quest_noteDiscussion_reopen2
            NoteComment.Action.HIDDEN -> R.string.quest_noteDiscussion_hide2
            else -> 0
        }
    }
}
