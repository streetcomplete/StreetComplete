package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.databinding.FormLeaveNoteBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestAnswerBinding
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/** Bottom sheet fragment with which the user can leave a note instead of solving the quest */
class LeaveNoteInsteadFragment : AbstractCreateNoteFragment(), IsShowingQuestDetails {

    private val noteEditsController: NoteEditsController by inject()

    private var _binding: FragmentQuestAnswerBinding? = null
    private val binding: FragmentQuestAnswerBinding get() = _binding!!

    override val bottomSheetContainer get() = binding.bottomSheetContainer
    override val bottomSheet get() = binding.bottomSheet
    override val scrollViewChild get() = binding.scrollViewChild
    override val bottomSheetTitle get() = binding.speechBubbleTitleContainer
    override val bottomSheetContent get() = binding.speechbubbleContentContainer
    override val floatingBottomView get() = binding.okButton
    override val backButton get() = binding.closeButton
    override val okButton get() = binding.okButton

    private val contentBinding by viewBinding(FormLeaveNoteBinding::bind, R.id.content)

    override val noteInput get() = contentBinding.noteInput

    interface Listener {
        fun onCreatedNote(questKey: OsmQuestKey?, position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private lateinit var questTitle: String
    override lateinit var questKey: OsmQuestKey
    private lateinit var position: LatLon

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        val args = requireArguments()
        questTitle = args.getString(ARG_QUEST_TITLE)!!
        questKey = Json.decodeFromString(args.getString(ARG_QUEST_KEY)!!)
        position = Json.decodeFromString(args.getString(ARG_POSITION)!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuestAnswerBinding.inflate(inflater, container, false)
        inflater.inflate(R.layout.form_leave_note, binding.content)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonPanel.isGone = true
        contentBinding.descriptionLabel.isGone = true
        binding.titleLabel.text = getString(R.string.map_btn_create_note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onComposedNote(text: String, imagePaths: List<String>) {

        var fullText = "Unable to answer \"$questTitle\""
        fullText += " for https://osm.org/${questKey.elementType.name.lowercase()}/${questKey.elementId}"
        fullText += " via ${ApplicationConstants.USER_AGENT}:\n\n$text"

        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteEditsController.add(0, NoteEditAction.CREATE, position, fullText, imagePaths)
            }
            listener?.onCreatedNote(questKey, position)
        }
    }

    companion object {
        private const val ARG_QUEST_TITLE = "questTitle"
        private const val ARG_QUEST_KEY = "questKey"
        private const val ARG_POSITION = "position"

        fun create(questKey: OsmQuestKey, questTitle: String, position: LatLon): LeaveNoteInsteadFragment {
            val f = LeaveNoteInsteadFragment()
            f.arguments = bundleOf(
                ARG_QUEST_KEY to Json.encodeToString(questKey),
                ARG_QUEST_TITLE to questTitle,
                ARG_POSITION to Json.encodeToString(position)
            )
            return f
        }
    }
}
