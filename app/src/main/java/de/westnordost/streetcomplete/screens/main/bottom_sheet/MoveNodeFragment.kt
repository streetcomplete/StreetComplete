package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.FragmentCreateNoteBinding
import de.westnordost.streetcomplete.overlays.IsShowingElement
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/** Fragment that lets the user move an OSM node */
class MoveNodeFragment :
    Fragment(R.layout.fragment_create_note), IsCloseableBottomSheet, IsShowingElement {

    private val binding by viewBinding(FragmentCreateNoteBinding::bind)
    private val bottomSheetBinding get() = binding.questAnswerLayout
    private val okButton get() = bottomSheetBinding.okButton
    private val okButtonContainer get() = bottomSheetBinding.okButtonContainer

    private val elementEditsController: ElementEditsController by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val overlayRegistry: OverlayRegistry by inject()

    override val elementKey: ElementKey by lazy { ElementKey(node.type, node.id) }

    private lateinit var node: Node
    private lateinit var editType: ElementEditType

    private val hasChanges get() = getPosition() != node.position
    private val isFormComplete get() = hasChanges //&& getPosition()?.distanceTo(node.position)?.let { it > 2.0 } ?: false // this is ugly

    interface Listener {
        fun getMapPositionAt(screenPos: Point): LatLon?

        fun onMovedNode(editType: ElementEditType, position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private fun getPosition(): LatLon? {
        val createNoteMarker = binding.markerCreateLayout.createNoteMarker
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        return listener?.getMapPositionAt(screenPos)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        node = Json.decodeFromString(args.getString(ARG_NODE)!!)
        editType = questTypeRegistry.getByName(args.getString(ARG_QUESTTYPE)!!) as? OsmElementQuestType<*>
            ?: overlayRegistry.getByName(args.getString(ARG_QUESTTYPE)!!)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        okButton.setOnClickListener { onClickOk() }
        okButtonContainer.popIn()
        okButton.popIn()
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

    private fun onClickOk() {
        val pos = getPosition() ?: return
        viewLifecycleScope.launch {
            val action = MoveNodeAction(pos)
            elementEditsController.add(editType, node, ElementPointGeometry(node.position), "survey", action)
            listener?.onMovedNode(editType, pos)
        }
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        // todo: maybe move map to this position? what would a "normal" person expect?
        return false
    }

    // todo:
    //  nicer view with that crosshair and distance?
    //   don't use the note layout!
    //  hide ok button if not moved, or moved very far -> see MainFragment.onMapIsChanging
    //  confirm large move
    //  confirm very small move?
    //  switch to satellite view??

    @UiThread override fun onClickClose(onConfirmed: () -> Unit) {
        if (!hasChanges) {
            onConfirmed()
        } else {
            activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_discard_title)
                    .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                        onConfirmed()
                    }
                    .setNegativeButton(R.string.short_no_answer_on_button, null)
                    .show()
            }
        }
    }

    companion object {
        private const val ARG_NODE = "node"
        private const val ARG_QUESTTYPE = "quest_type"

        fun create(elementEditType: ElementEditType, node: Node): MoveNodeFragment {
            val f = MoveNodeFragment()
            f.arguments = bundleOf(
                ARG_NODE to Json.encodeToString(node),
                ARG_QUESTTYPE to elementEditType.name
            )
            return f
        }
    }
}
