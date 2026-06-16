package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.content.res.Configuration
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.graphics.toPointF
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.databinding.FragmentCreateNoteBinding
import de.westnordost.streetcomplete.quests.note_comments.NoteForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/** Bottom sheet fragment with which the user can create a new note, including moving the note */
class CreateNoteFragment : AbstractCreateNoteFragment() {

    private val noteEditsController: NoteEditsController by inject()
    private val fileSystem: FileSystem by inject()

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding: FragmentCreateNoteBinding get() = _binding!!

    private val bottomSheetBinding get() = binding.questAnswerLayout

    override val bottomSheetContainer get() = bottomSheetBinding.bottomSheetContainer
    override val bottomSheet get() = bottomSheetBinding.bottomSheet
    override val scrollViewChild get() = bottomSheetBinding.scrollViewChild
    override val bottomSheetTitle get() = bottomSheetBinding.speechBubbleTitleContainer
    override val bottomSheetContent get() = bottomSheetBinding.speechbubbleContentContainer
    override val floatingBottomView get() = bottomSheetBinding.okButton
    override val okButton get() = bottomSheetBinding.okButton
    override val okButtonContainer get() = bottomSheetBinding.okButtonContainer
    private lateinit var content: ComposeView

    private var hasGpxAttached: Boolean = false

    interface Listener {
        fun getMapPositionAt(screenPos: PointF): LatLon?
        fun getRecordedTrack(): List<Trackpoint>?

        fun onCreatedNote(position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasGpxAttached = arguments?.getBoolean(ARG_HAS_GPX_ATTACHED) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNoteBinding.inflate(inflater, container, false)
        content = ComposeView(inflater.context)
        bottomSheetBinding.content.addView(content)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addImagesEnabled = requireContext().packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)

        bottomSheetBinding.buttonPanel.isGone = true

        if (savedInstanceState == null) {
            binding.markerCreateLayout.markerLayoutContainer.startAnimation(createFallDownAnimation())
        }

        bottomSheetBinding.questHeader.content { Surface {
            QuestHeader(
                title = stringResource(Res.string.map_btn_create_note),
                subtitle = null,
                hintText =
                    stringResource(Res.string.create_new_note_description) +
                    "\n" +
                    stringResource(Res.string.create_new_note_hint),
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
                isGpxAttached = false,
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.markerCreateLayout.centeredMarkerLayout.setPadding(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        )
    }

    private fun createFallDownAnimation(): Animation {
        val a = AnimationSet(false)
        a.startOffset = 200

        val ta = TranslateAnimation(0, 0f, 0, 0f, 1, -0.2f, 0, 0f)
        ta.interpolator = BounceInterpolator()
        ta.duration = 400
        a.addAnimation(ta)

        val aa = AlphaAnimation(0f, 1f)
        aa.interpolator = AccelerateInterpolator()
        aa.duration = 200
        a.addAnimation(aa)

        return a
    }

    override fun isRejectingClose() = super.isRejectingClose() || hasGpxAttached

    override fun onDiscard() {
        super.onDiscard()
        binding.markerCreateLayout.markerLayoutContainer.visibility = View.INVISIBLE
    }

    override fun onComposedNote(text: String, imagePaths: List<String>) {
        /* pressing once on "OK" should first only close the keyboard, so that the user can review
           the position of the note he placed */
        // TODO Compose: When layout including OK button has been migrated to Compose, uncomment
        //if (isImeVisible()) {
        //    LocalSoftwareKeyboardController.current?.hide()
        //}

        val createNoteMarker = binding.markerCreateLayout.pin.root
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        val position = listener?.getMapPositionAt(screenPos.toPointF()) ?: return

        binding.markerCreateLayout.markerLayoutContainer.visibility = View.INVISIBLE

        val fullText = "$text\n\nvia ${ApplicationConstants.USER_AGENT}"
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val recordedTrack =
                    if (hasGpxAttached) listener?.getRecordedTrack().orEmpty() else emptyList()
                noteEditsController.add(0, NoteEditAction.CREATE, position, fullText, imagePaths, recordedTrack)
            }
        }

        listener?.onCreatedNote(position)
    }

    companion object {
        private const val ARG_HAS_GPX_ATTACHED = "hasGpxAttached"

        fun create(hasGpxAttached: Boolean) = CreateNoteFragment().also {
            it.arguments = bundleOf(ARG_HAS_GPX_ATTACHED to hasGpxAttached)
        }
    }
}
