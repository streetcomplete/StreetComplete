package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.FragmentMoveNodeBinding
import de.westnordost.streetcomplete.overlays.IsShowingElement
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.getPinIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.measure.MeasureDisplayUnit
import de.westnordost.streetcomplete.screens.measure.MeasureDisplayUnitFeetInch
import de.westnordost.streetcomplete.screens.measure.MeasureDisplayUnitMeter
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.concurrent.FutureTask

/** Fragment that lets the user move an OSM node */
class MoveNodeFragment :
    Fragment(R.layout.fragment_create_note), IsCloseableBottomSheet, IsShowingElement, IsMapPositionAware {

    private var _binding: FragmentMoveNodeBinding? = null
    private val binding: FragmentMoveNodeBinding get() = _binding!!
    private val okButton get() = binding.okButton
    private val okButtonContainer get() = binding.okButtonContainer

    private val elementEditsController: ElementEditsController by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val countryInfos: CountryInfos by inject()

    override val elementKey: ElementKey by lazy { ElementKey(node.type, node.id) }

    private lateinit var node: Node
    private lateinit var editType: ElementEditType
    private lateinit var displayUnit: MeasureDisplayUnit

    private val hasChanges get() = getPosition() != node.position

    interface Listener {
        fun getMapPositionAt(screenPos: Point): LatLon?

        fun onMovedNode(editType: ElementEditType, position: LatLon)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        node = Json.decodeFromString(args.getString(ARG_NODE)!!)
        editType = questTypeRegistry.getByName(args.getString(ARG_QUEST_TYPE)!!) as? OsmElementQuestType<*>
            ?: overlayRegistry.getByName(args.getString(ARG_QUEST_TYPE)!!)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoveNodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        okButton.setOnClickListener { onClickOk() }
        binding.cancelButton.setOnClickListener { activity?.onBackPressed() }
        val countryInfo = countryInfos.getByLocation(countryBoundaries.get(), node.position.longitude, node.position.latitude)
        displayUnit = if (countryInfo.lengthUnits.firstOrNull() == LengthUnit.FOOT_AND_INCH)
                MeasureDisplayUnitFeetInch(1)
            else
                MeasureDisplayUnitMeter(2)
        binding.createNoteIconView.setImageResource(editType.icon)
    }

    private fun getPosition(): LatLon? {
        val createNoteMarker = binding.createNoteMarker
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        return listener?.getMapPositionAt(screenPos)
    }

    private fun onClickOk() {
        val pos = getPosition() ?: return
        if (!checkIsDistanceOkAndUpdateText(pos)) return
        viewLifecycleScope.launch {
            val action = MoveNodeAction(pos)
            elementEditsController.add(editType, node, ElementPointGeometry(node.position), "survey", action)
            listener?.onMovedNode(editType, pos)
        }
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double) = false

    @UiThread override fun onMapMoved(position: LatLon) {
        if (checkIsDistanceOkAndUpdateText(position))
            okButtonContainer.popIn()
        else
            okButtonContainer.popOut()
    }

    private fun checkIsDistanceOkAndUpdateText(position: LatLon): Boolean {
        val moveDistance = position.distanceTo(node.position)
        return when {
            moveDistance < MIN_MOVE_DISTANCE -> {
                binding.titleLabel.setText(R.string.node_moved_not_far_enough)
                false
            }
            moveDistance > MAX_MOVE_DISTANCE -> {
                binding.titleLabel.setText(R.string.node_moved_too_far)
                false
            }
            else -> {
                binding.titleLabel.text = resources.getString(R.string.node_moved,displayUnit.format(moveDistance.toFloat()))
                true
            }
        }
    }

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
        private const val ARG_QUEST_TYPE = "quest_type"

        fun create(elementEditType: ElementEditType, node: Node): MoveNodeFragment {
            val f = MoveNodeFragment()
            f.arguments = bundleOf(
                ARG_NODE to Json.encodeToString(node),
                ARG_QUEST_TYPE to elementEditType.name
            )
            return f
        }
    }
}

// Require a minimum distance for two reasons:
// 1. The map is not perfectly precise, especially displayed road widths may be off by a few meters,
//     so it may be hard to tell whether something really is misplaced (e.g. bench along a path)
//     without good aerial imagery.
// 2. The value added by moving nodes by such small distance, even if correct, is rather low.
private const val MIN_MOVE_DISTANCE = 2.0
// Move node functionality is meant for fixing slightly misplaced elements. If something moved far
// away, it is reasonable to assume there are more substantial changes required, also to nearby elements.
private const val MAX_MOVE_DISTANCE = 30.0
