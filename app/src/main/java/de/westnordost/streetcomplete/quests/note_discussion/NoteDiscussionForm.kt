package de.westnordost.streetcomplete.quests.note_discussion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.NotesModule
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionContentBinding
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionItemBinding
import de.westnordost.streetcomplete.databinding.QuestNoteDiscussionItemsBinding
import de.westnordost.streetcomplete.ktx.createBitmap
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.RoundRectOutlineProvider
import java.io.File
import java.time.Instant
import javax.inject.Inject

class NoteDiscussionForm : AbstractQuestFormAnswerFragment<NoteAnswer>() {

    override val contentLayoutResId = R.layout.quest_note_discussion_content
    private val binding by contentViewBinding(QuestNoteDiscussionContentBinding::bind)

    override val defaultExpanded = false

    private lateinit var anonAvatar: Bitmap

    @Inject internal lateinit var noteSource: NotesWithEditsSource

    private val attachPhotoFragment get() =
        childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as? AttachPhotoFragment

    private val noteText: String get() = binding.noteInput.text?.toString().orEmpty().trim()

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_noteDiscussion_no) { skipQuest() }
    )

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noteInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })

        otherAnswersButton?.visibility = View.GONE

        anonAvatar = requireContext().getDrawable(R.drawable.ic_osm_anon_avatar)!!.createBitmap()

        val osmNoteQuestKey = questKey as OsmNoteQuestKey
        inflateNoteDiscussion(noteSource.get(osmNoteQuestKey.noteId)!!.comments)

        if (savedInstanceState == null) {
            childFragmentManager.commit { add<AttachPhotoFragment>(R.id.attachPhotoFragment) }
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
        applyAnswer(NoteAnswer(noteText, attachPhotoFragment?.imagePaths.orEmpty()))
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose(): Boolean {
        val f = attachPhotoFragment
        val hasPhotos = f != null && f.imagePaths.isNotEmpty()
        return hasPhotos || noteText.isNotEmpty()
    }

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

        override fun onBind(comment: NoteComment) {
            val dateDescription = DateUtils.getRelativeTimeSpanString(comment.timestamp, Instant.now().toEpochMilli(), MINUTE_IN_MILLIS)
            val userName = if (comment.user != null) comment.user.displayName else getString(R.string.quest_noteDiscussion_anonymous)

            val commentActionResourceId = comment.action.actionResourceId
            val hasNoteAction = commentActionResourceId != 0
            itemBinding.commentStatusText.isGone = !hasNoteAction
            if (hasNoteAction) {
                itemBinding.commentStatusText.text = getString(commentActionResourceId, userName, dateDescription)
            }

            val hasComment = comment.text?.isNotEmpty() == true
            itemBinding.commentView.isGone = !hasComment
            if (hasComment) {
                itemBinding.commentText.text = comment.text
                itemBinding.commentInfoText.text = getString(R.string.quest_noteDiscussion_comment2, userName, dateDescription)

                val bitmap = comment.user?.avatar ?: anonAvatar
                itemBinding.commentAvatarImage.setImageBitmap(bitmap)
            }
        }

        private val User.avatar: Bitmap? get() {
            val cacheDir = NotesModule.getAvatarsCacheDirectory(requireContext())
            val file = File(cacheDir.toString() + File.separator + id)
            return if (file.exists()) BitmapFactory.decodeFile(file.path) else null
        }

        private val NoteComment.Action.actionResourceId get() = when (this) {
            NoteComment.Action.CLOSED -> R.string.quest_noteDiscussion_closed2
            NoteComment.Action.REOPENED -> R.string.quest_noteDiscussion_reopen2
            NoteComment.Action.HIDDEN -> R.string.quest_noteDiscussion_hide2
            else -> 0
        }
    }

    override fun isFormComplete(): Boolean = noteText.isNotEmpty()
}
