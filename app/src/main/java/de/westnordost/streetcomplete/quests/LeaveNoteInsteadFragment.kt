package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestGroup
import kotlinx.android.synthetic.main.form_leave_note.*
import kotlinx.android.synthetic.main.fragment_quest_answer.*

/** Bottom sheet fragment with which the user can leave a note instead of solving the quest */
class LeaveNoteInsteadFragment : AbstractCreateNoteFragment(), IsShowingQuestDetails {

    interface Listener {
        fun onCreatedNoteInstead(questId: Long, group: QuestGroup, questTitle: String, note: String, imagePaths: List<String>?)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override val layoutResId = R.layout.fragment_quest_answer

    private lateinit var questTitle: String
    override var questId: Long = 0L
    override var questGroup: QuestGroup = QuestGroup.OSM

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        val args = requireArguments()
        questTitle = args.getString(ARG_QUEST_TITLE)!!
        questId = args.getLong(ARG_QUEST_ID)
        questGroup = QuestGroup.valueOf(args.getString(ARG_QUEST_GROUP)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLabel.text = getString(R.string.map_btn_create_note)
        descriptionLabel.text = null
    }

    override fun onComposedNote(text: String, imagePaths: List<String>?) {
        listener?.onCreatedNoteInstead(questId, questGroup, questTitle, text, imagePaths)
    }

    companion object {
        private const val ARG_QUEST_TITLE = "questTitle"
        private const val ARG_QUEST_ID = "questId"
        private const val ARG_QUEST_GROUP = "questGroup"

        @JvmStatic
        fun create(questId: Long, group: QuestGroup, questTitle: String): LeaveNoteInsteadFragment {
            val f = LeaveNoteInsteadFragment()
            f.arguments = bundleOf(
                ARG_QUEST_GROUP to group.name,
                ARG_QUEST_ID to questId,
                ARG_QUEST_TITLE to questTitle
            )
            return f
        }
    }
}
