package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FormLeaveNoteBinding
import de.westnordost.streetcomplete.databinding.FragmentCreateNoteBinding
import de.westnordost.streetcomplete.quests.tagsOk
import de.westnordost.streetcomplete.quests.toTags
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.android.ext.android.inject

/** Abstract base class for a bottom sheet that lets the user create a note */
class CreatePoiFragment : AbstractBottomSheetFragment() {

    private val elementEditsController: ElementEditsController by inject()

    private val okButtonContainer: View get() = bottomSheetBinding.okButtonContainer
    private val okButton: View get() = bottomSheetBinding.okButton

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding: FragmentCreateNoteBinding get() = _binding!!

    private val bottomSheetBinding get() = binding.questAnswerLayout

    override val bottomSheetContainer get() = bottomSheetBinding.bottomSheetContainer
    override val bottomSheet get() = bottomSheetBinding.bottomSheet
    override val scrollViewChild get() = bottomSheetBinding.scrollViewChild
    override val bottomSheetTitle get() = bottomSheetBinding.speechBubbleTitleContainer
    override val bottomSheetContent get() = bottomSheetBinding.speechbubbleContentContainer
    override val floatingBottomView get() = bottomSheetBinding.okButton
    override val floatingBottomView2 get() = bottomSheetBinding.hideButton
    override val backButton get() = bottomSheetBinding.closeButton

    private val contentBinding by viewBinding(FormLeaveNoteBinding::bind, R.id.content)
    private val tagsInput get() = contentBinding.noteInput
    private val tagsText get() = tagsInput.nonBlankTextOrNull
    private var tempText = ""

    // keep the names from note, there is nothing note-specific happening anyway
    interface Listener {
        fun getMapPositionAt(screenPos: Point): LatLon?

        fun onCreatedNote(position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tempText = arguments?.getString(ARG_PREFILLED_TAGS) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNoteBinding.inflate(inflater, container, false)
        inflater.inflate(R.layout.form_leave_note, bottomSheetBinding.content)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.titleLabel.text = arguments?.getString(ARG_NAME)
        contentBinding.hintLabel.setText(R.string.create_poi_enter_tags)
        tagsInput.setText(tempText)
        tagsInput.doAfterTextChanged { updateOkButtonEnablement() }
        okButton.setOnClickListener { onClickOk() }

        updateOkButtonEnablement()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickOk() {
        if (tagsText?.let { tagsOk(it) } != true) return
        val tags = tagsText!!.toTags()

        val createNoteMarker = binding.markerCreateLayout.createNoteMarker
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        val position = listener?.getMapPositionAt(screenPos) ?: return

        // need some editType
        elementEditsController.add(CreatePoiEditType(), Node(0, position), ElementPointGeometry(position), "survey", CreateNodeAction(position, tags))
        listener?.onCreatedNote(position)
    }

    override fun isRejectingClose() =
        tagsText != null

    private fun updateOkButtonEnablement() {
        if (tagsText != null && tagsOk(tagsText!!)) {
            okButtonContainer.popIn()
        } else {
            okButtonContainer.popOut()
        }
    }

    companion object {
        private const val ARG_PREFILLED_TAGS = "prefilled_tags"
        private const val ARG_NAME = "feature_name"

        fun create(feature: Feature?) = CreatePoiFragment().also {
            feature?.let { recentFeatures.add(it) }
            val tagText = feature?.addTags?.map { it.key + "=" + it.value }?.joinToString("\n")
            it.arguments = bundleOf(ARG_PREFILLED_TAGS to tagText, ARG_NAME to feature?.name)
        }
        val recentFeatures = linkedSetOf<Feature>()
    }
}

class CreatePoiEditType: ElementEditType {
    override val icon: Int = R.drawable.ic_quest_create_note
    override val title: Int = R.string.create_poi
    override val wikiLink: String? = null
    override val changesetComment: String = "Add node"
}
