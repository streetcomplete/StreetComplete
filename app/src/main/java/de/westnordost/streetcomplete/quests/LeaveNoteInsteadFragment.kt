package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.AbstractCreateNoteFragment
import kotlinx.android.synthetic.main.form_leave_note.*
import kotlinx.android.synthetic.main.fragment_quest_answer.*

class LeaveNoteInsteadFragment : AbstractCreateNoteFragment() {

    override val layoutResId = R.layout.fragment_quest_answer

    private val questAnswerComponent: QuestAnswerComponent = QuestAnswerComponent()
    private lateinit var questTitle: String

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        questAnswerComponent.onCreate(arguments)
        questTitle = arguments!!.getString(ARG_QUEST_TITLE)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLabel.text = getString(R.string.map_btn_create_note)
        descriptionLabel.text = getString(R.string.quest_leave_new_note_description)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questAnswerComponent.onAttach(context as OsmQuestAnswerListener)
    }

    override fun onLeaveNote(text: String, imagePaths: List<String>?) {
        questAnswerComponent.onLeaveNote(questTitle, text, imagePaths)
    }

    companion object {
        const val ARG_QUEST_TITLE = "questTitle"
    }
}
