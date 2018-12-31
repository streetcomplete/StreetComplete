package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.AbstractCreateNoteFragment

class LeaveNoteInsteadFragment : AbstractCreateNoteFragment() {

    private val questAnswerComponent: QuestAnswerComponent = QuestAnswerComponent()
	private var questTitle: String? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setTitle(R.string.map_btn_create_note)
        setDescription(R.string.quest_leave_new_note_description)

        return view
    }

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        questAnswerComponent.onCreate(arguments)
        questTitle = arguments!!.getString(ARG_QUEST_TITLE)
    }

    override fun onAttach(ctx: Context?) {
        super.onAttach(ctx)
        questAnswerComponent.onAttach(ctx as OsmQuestAnswerListener?)
    }

    override fun onLeaveNote(text: String, imagePaths: ArrayList<String>?) {
        questAnswerComponent.onLeaveNote(questTitle, text, imagePaths)
    }

    override fun getLayoutResId() = R.layout.fragment_quest_answer

    companion object {
        const val ARG_QUEST_TITLE = "questTitle"
    }
}
