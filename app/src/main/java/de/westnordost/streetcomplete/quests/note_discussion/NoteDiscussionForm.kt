package de.westnordost.streetcomplete.quests.note_discussion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import kotlinx.android.synthetic.main.quest_buttonpanel_note_discussion.*
import kotlinx.android.synthetic.main.quest_note_discussion_content.*

class NoteDiscussionForm : AbstractQuestAnswerFragment() {

    private lateinit var anonAvatar: Bitmap

    @Inject internal lateinit var noteDb: OsmNoteQuestDao

    private val attachPhotoFragment get() =
	    childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as? AttachPhotoFragment

    private val noteText: String get() = noteInput.text.toString().trim()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setContentView(R.layout.quest_note_discussion_content)

        setButtonsView(R.layout.quest_buttonpanel_note_discussion)
        okButton.setOnClickListener { onClickOk() }
        noButton.setOnClickListener { skipQuest() }

        noteInput.addTextChangedListener(TextChangedWatcher(TextChangedWatcher.Listener { this.updateOkButtonEnablement() }))

        buttonOtherAnswers.visibility = View.GONE

        updateOkButtonEnablement()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        anonAvatar = BitmapUtil.createBitmapFrom(resources.getDrawable(R.drawable.ic_osm_anon_avatar))

        inflateNoteDiscussion(noteDb.get(questId).note.comments)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
	            .add(R.id.attachPhotoFragment, AttachPhotoFragment())
	            .commit()
        }
    }

    private fun inflateNoteDiscussion(comments: List<NoteComment>) {
        val layout = view!!.findViewById<LinearLayout>(R.id.scrollViewChild)
        val discussionView = layoutInflater.inflate(R.layout.quest_note_discussion_items, layout, false) as RecyclerView

        discussionView.isNestedScrollingEnabled = false
        discussionView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        discussionView.adapter = NoteCommentListAdapter(comments)

        layout.addView(discussionView, 0)
    }

    private fun onClickOk() {
        val answer = Bundle()
        answer.putString(TEXT, noteText)
	    attachPhotoFragment?.let { answer.putStringArrayList(IMAGE_PATHS, it.imagePaths) }
        applyAnswer(answer)
    }

    public override fun onDiscard() {
	    attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose(): Boolean {
        val f = attachPhotoFragment
        val hasPhotos = f != null && !f.imagePaths.isEmpty()
        return hasPhotos || noteText.isNotEmpty()
    }

    private fun updateOkButtonEnablement() {
        okButton.isEnabled = noteText.isNotEmpty()
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

            if (comment.text != null && !comment.text.isEmpty()) {
                commentContainer.visibility = View.VISIBLE
                commentText.text = comment.text
                commentInfo.text = getString(R.string.quest_noteDiscussion_comment2, userName, dateDescription)

                var bitmap = anonAvatar
                if (comment.user != null) {
                    val avatarFile = File(OsmModule.getAvatarsCacheDirectory(context!!).toString() + File.separator + comment.user.id)
                    if (avatarFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(avatarFile.path)
                    }
                }
                val avatarDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                avatarDrawable.isCircular = true
                commentAvatar.setImageDrawable(avatarDrawable)
            } else {
                commentContainer.visibility = View.GONE
            }
        }

        private val NoteComment.Action.actionResourceId get() = when (this) {
	        NoteComment.Action.CLOSED -> R.string.quest_noteDiscussion_closed2
	        NoteComment.Action.REOPENED -> R.string.quest_noteDiscussion_reopen2
	        NoteComment.Action.HIDDEN -> R.string.quest_noteDiscussion_hide2
	        else -> 0
        }
    }

    companion object {
        const val TEXT = "text"
        const val IMAGE_PATHS = "image_paths"
    }
}
