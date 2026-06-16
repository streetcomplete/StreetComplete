package de.westnordost.streetcomplete.quests

import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.databinding.FragmentQuestAnswerBinding
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.AbstractCreateNoteFragment
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/** Bottom sheet fragment with which the user can leave a note instead of solving the quest/edit */
class LeaveNoteInsteadFragment : AbstractCreateNoteFragment() {

    private val noteEditsController: NoteEditsController by inject()
    private val fileSystem: FileSystem by inject()

    private var _binding: FragmentQuestAnswerBinding? = null
    private val binding: FragmentQuestAnswerBinding get() = _binding!!

    override val bottomSheetContainer get() = binding.bottomSheetContainer
    override val bottomSheet get() = binding.bottomSheet
    override val scrollViewChild get() = binding.scrollViewChild
    override val bottomSheetTitle get() = binding.speechBubbleTitleContainer
    override val bottomSheetContent get() = binding.speechbubbleContentContainer
    override val floatingBottomView get() = binding.okButtonContainer
    override val okButton get() = binding.okButton
    override val okButtonContainer get() = binding.okButtonContainer
    private lateinit var content: ComposeView

    interface Listener {
        fun onCreatedNote(position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private var leaveNoteContext: String? = null
    private lateinit var position: LatLon
    private lateinit var elementType: ElementType
    private var elementId: Long = 0L

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        val args = requireArguments()
        leaveNoteContext = args.getString(ARG_LEAVE_NOTE_CONTEXT)
        elementType = ElementType.valueOf(args.getString(ARG_ELEMENT_TYPE)!!)
        elementId = args.getLong(ARG_ELEMENT_ID)
        position = Json.decodeFromString(args.getString(ARG_POSITION)!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuestAnswerBinding.inflate(inflater, container, false)
        content = ComposeView(inflater.context)
        binding.content.addView(content)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addImagesEnabled = requireContext().packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)

        binding.buttonPanel.isGone = true
        binding.questHeader.content { Surface {
            QuestHeader(
                title = stringResource(Res.string.map_btn_create_note),
                subtitle = null,
                hintText = stringResource(Res.string.create_new_note_hint),
                hintImages = emptyList()
            )
        } }
        content.content { Surface {
            NoteForm(
                text = noteText.value,
                onTextChange = {
                    noteText.value = it
                    updateOkButtonEnablement()
                },
                addImagesEnabled = addImagesEnabled,
                onDeleteImage = ::deleteImage,
                onTakePhoto = { takePhoto() },
                fileSystem = fileSystem,
                imagePaths = noteImagePaths.value,
            )
        } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onComposedNote(text: String, imagePaths: List<String>) {
        val fullText = mutableListOf<String>()
        leaveNoteContext?.let { fullText += it }
        fullText += "– https://osm.org/${elementType.name.lowercase()}/$elementId"
        fullText += "via ${ApplicationConstants.USER_AGENT}:\n\n$text"

        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteEditsController.add(0, NoteEditAction.CREATE, position, fullText.joinToString(" "), imagePaths)
            }
            listener?.onCreatedNote(position)
        }
    }

    companion object {
        private const val ARG_LEAVE_NOTE_CONTEXT = "questTitle"
        private const val ARG_ELEMENT_TYPE = "elementType"
        private const val ARG_ELEMENT_ID = "elementId"
        private const val ARG_POSITION = "position"

        fun create(elementType: ElementType, elementId: Long, leaveNoteContext: String?, position: LatLon): LeaveNoteInsteadFragment {
            val f = LeaveNoteInsteadFragment()
            f.arguments = bundleOf(
                ARG_ELEMENT_TYPE to elementType.name,
                ARG_ELEMENT_ID to elementId,
                ARG_LEAVE_NOTE_CONTEXT to leaveNoteContext,
                ARG_POSITION to Json.encodeToString(position)
            )
            return f
        }
    }
}
