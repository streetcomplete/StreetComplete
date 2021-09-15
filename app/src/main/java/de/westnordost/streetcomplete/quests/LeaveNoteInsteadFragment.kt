package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.databinding.FormLeaveNoteBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestAnswerBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Bottom sheet fragment with which the user can leave a note instead of solving the quest */
class LeaveNoteInsteadFragment : AbstractCreateNoteFragment(), IsShowingQuestDetails {

    override val layoutResId = R.layout.fragment_quest_answer

    private val binding by viewBinding(FragmentQuestAnswerBinding::bind)

    override val bottomSheetContainer get() = binding.bottomSheetContainer
    override val bottomSheet get() = binding.bottomSheet
    override val scrollViewChild get() = binding.scrollViewChild
    override val bottomSheetTitle get() = binding.speechBubbleTitleContainer
    override val bottomSheetContent get() = binding.speechbubbleContentContainer
    override val floatingBottomView get() = binding.okButton
    override val backButton get() = binding.closeButton
    override val buttonPanel get() = binding.buttonPanel

    private val contentBinding by viewBinding(FormLeaveNoteBinding::bind)

    override val noteInput get() = contentBinding.noteInput

    interface Listener {
        fun onCreatedNoteInstead(questKey: QuestKey, questTitle: String, note: String, imagePaths: List<String>)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private lateinit var questTitle: String
    override lateinit var questKey: QuestKey

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        val args = requireArguments()
        questTitle = args.getString(ARG_QUEST_TITLE)!!
        questKey = Json.decodeFromString(args.getString(ARG_QUEST_KEY)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleLabel.text = getString(R.string.map_btn_create_note)
        contentBinding.descriptionLabel.text = null
    }

    override fun onComposedNote(text: String, imagePaths: List<String>) {
        listener?.onCreatedNoteInstead(questKey, questTitle, text, imagePaths)
    }

    companion object {
        private const val ARG_QUEST_TITLE = "questTitle"
        private const val ARG_QUEST_KEY = "questKey"

        fun create(questKey: QuestKey, questTitle: String): LeaveNoteInsteadFragment {
            val f = LeaveNoteInsteadFragment()
            f.arguments = bundleOf(
                ARG_QUEST_KEY to Json.encodeToString(questKey),
                ARG_QUEST_TITLE to questTitle
            )
            return f
        }
    }
}
