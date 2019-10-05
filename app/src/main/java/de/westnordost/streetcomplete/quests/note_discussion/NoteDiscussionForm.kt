package de.westnordost.streetcomplete.quests.note_discussion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.io.File
import java.util.Date

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.OsmModule
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.util.BitmapUtil
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.ListAdapter

import android.text.format.DateUtils.MINUTE_IN_MILLIS
import de.westnordost.osmapi.user.User
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_buttonpanel_note_discussion.*
import kotlinx.android.synthetic.main.quest_note_discussion_content.*

class NoteDiscussionForm : AbstractQuestAnswerFragment<NoteAnswer>() {

    override val contentLayoutResId = R.layout.quest_note_discussion_content
    override val buttonsResId = R.layout.quest_buttonpanel_note_discussion

    private lateinit var anonAvatar: Bitmap

    @Inject internal lateinit var noteDb: OsmNoteQuestDao

    private val attachPhotoFragment get() =
        childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as? AttachPhotoFragment

    private val noteText: String get() = noteInput?.text?.toString().orEmpty().trim()

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doneButton.setOnClickListener { onClickOk() }
        noButton.setOnClickListener { skipQuest() }

        noteInput.addTextChangedListener(TextChangedWatcher { updateDoneButtonEnablement() })

        otherAnswersButton.visibility = View.GONE

        updateDoneButtonEnablement()

        anonAvatar = BitmapUtil.createBitmapFrom(resources.getDrawable(R.drawable.ic_osm_anon_avatar))

        inflateNoteDiscussion(noteDb.get(questId)!!.note.comments)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.attachPhotoFragment, AttachPhotoFragment())
                .commit()
        }
    }

    private fun inflateNoteDiscussion(comments: List<NoteComment>) {
        val discussionView = layoutInflater.inflate(R.layout.quest_note_discussion_items, scrollViewChild, false) as RecyclerView

        discussionView.isNestedScrollingEnabled = false
        discussionView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        discussionView.adapter = NoteCommentListAdapter(comments)

        scrollViewChild.addView(discussionView, 0)
    }

    private fun onClickOk() {
        applyAnswer(NoteAnswer(noteText, attachPhotoFragment?.imagePaths))
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose(): Boolean {
        val f = attachPhotoFragment
        val hasPhotos = f != null && !f.imagePaths.isEmpty()
        return hasPhotos || noteText.isNotEmpty()
    }

    private fun updateDoneButtonEnablement() {
        doneButton.isEnabled = noteText.isNotEmpty()
    }


    private inner class NoteCommentListAdapter(list: List<NoteComment>) :
        ListAdapter<NoteComment>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder<NoteComment> {
            return NoteCommentViewHolder(
                layoutInflater.inflate(R.layout.quest_note_discussion_item, parent, false)
            )
        }
    }

    private inner class NoteCommentViewHolder(itemView: View) :
        ListAdapter.ViewHolder<NoteComment>(itemView) {

        private val commentContainer: ViewGroup = itemView.findViewById(R.id.commentView)
        private val commentAvatar: ImageView = itemView.findViewById(R.id.commentAvatarImage)
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val commentInfo: TextView = itemView.findViewById(R.id.commentInfoText)
        private val commentStatusText: TextView = itemView.findViewById(R.id.commentStatusText)

        override fun onBind(with: NoteComment) {
            val comment = with

            val dateDescription = DateUtils.getRelativeTimeSpanString(comment.date.time, Date().time, MINUTE_IN_MILLIS)
            val userName = if (comment.user != null) comment.user.displayName else getString(R.string.quest_noteDiscussion_anonymous)

            val commentActionResourceId = comment.action.actionResourceId
            if (commentActionResourceId != 0) {
                commentStatusText.visibility = View.VISIBLE
                commentStatusText.text = getString(commentActionResourceId, userName, dateDescription)
            } else {
                commentStatusText.visibility = View.GONE
            }

            if (!comment.text.isNullOrEmpty()) {
                commentContainer.visibility = View.VISIBLE
                commentText.text = comment.text
                commentInfo.text = getString(R.string.quest_noteDiscussion_comment2, userName, dateDescription)

                val bitmap = comment.user?.avatar ?: anonAvatar
                val avatarDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                avatarDrawable.isCircular = true
                commentAvatar.setImageDrawable(avatarDrawable)
            } else {
                commentContainer.visibility = View.GONE
            }
        }

        private val User.avatar: Bitmap? get() {
            val file = File(OsmModule.getAvatarsCacheDirectory(context!!).toString() + File.separator + id)
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
