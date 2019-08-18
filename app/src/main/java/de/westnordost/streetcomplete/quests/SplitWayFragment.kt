package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.Injector

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.changes.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.sound.SoundFx
import de.westnordost.streetcomplete.util.SphericalEarthMath.*
import kotlinx.android.synthetic.main.fragment_split_way.*
import javax.inject.Inject

class SplitWayFragment : Fragment(), IsCloseableBottomSheet {

    interface Listener {
        fun onAddSplit(point: LatLon)
        fun onRemoveSplit(point: LatLon)
        fun onSplittedWay(osmQuestId: Long, splits: List<SplitPolylineAtPosition>)
    }
    private val splits: MutableList<Pair<SplitPolylineAtPosition, LatLon>> = mutableListOf()

    @Inject internal lateinit var soundFx: SoundFx

    private var osmQuestId: Long = 0L
    private lateinit var way: Way
    private lateinit var positions: List<OsmLatLon>

    private val hasChanges get() = splits.isNotEmpty()
    private val isFormComplete get() = splits.size >= if (way.isClosed()) 2 else 1

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        osmQuestId = arguments!!.getLong(ARG_QUEST_ID)
        way = arguments!!.getSerializable(ARG_WAY) as Way
        val elementGeometry = arguments!!.getSerializable(ARG_ELEMENT_GEOMETRY) as ElementGeometry
        positions = elementGeometry.polylines.single().map { OsmLatLon(it.latitude, it.longitude) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_split_way, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        okButton.setOnClickListener { onClickOk() }
        cancelButton.setOnClickListener { activity?.onBackPressed() }
        undoButton.setOnClickListener { onClickUndo() }

        undoButton.visibility = if (hasChanges) View.VISIBLE else View.INVISIBLE
        okButton.visibility = if (isFormComplete) View.VISIBLE else View.INVISIBLE

        if (savedInstanceState == null) {
            view.findViewById<View>(R.id.speechbubbleContentContainer).startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.inflate_answer_bubble)
            )
        }
    }

    private fun onClickOk() {
        (activity as Listener).onSplittedWay(osmQuestId, splits.map { it.first })
    }

    private fun onClickUndo() {
        if (splits.isNotEmpty()) {
            val item = splits.removeAt(splits.lastIndex)
            animateButtonVisibilities()
            (activity as? Listener)?.onRemoveSplit(item.second)
        }
    }

    @UiThread
    override fun onClickMapAt(position: LatLon, horizontalAccuracyInMeters: Double): Boolean {

        val splitWayCandidates = createSplits(position, horizontalAccuracyInMeters)
        // split point could be put on several places
        if (splitWayCandidates.size > 1) {
            context?.toast(R.string.quest_split_way_too_imprecise)
        }
        else if (splitWayCandidates.size == 1) {
            val splitWay = splitWayCandidates.single()
            val splitPosition = splitWay.pos

            // new split point is too close to existing split points
            if (splits.any { distance(it.second, splitPosition) < horizontalAccuracyInMeters } ) {
                context?.toast(R.string.quest_split_way_too_imprecise)
            } else {
                splits.add(Pair(splitWay, splitPosition))
                animateButtonVisibilities()
                soundFx.play(R.raw.snip)
                (activity as? Listener)?.onAddSplit(splitPosition)
            }
        }
        // always consume event. User should press the cancel button to exit
        return true
    }

    private fun createSplits(clickPosition: LatLon, horizontalAccuracyInMeters: Double): Set<SplitPolylineAtPosition> {
        val splitWaysAtNodes = createSplitsAtNodes(clickPosition, horizontalAccuracyInMeters)
        // if a split on a node is possible, do that and don't even check if a split on a way is also possible
        if (splitWaysAtNodes.isNotEmpty()) return splitWaysAtNodes
        return createSplitsForLines(clickPosition, horizontalAccuracyInMeters)
    }

    private fun createSplitsAtNodes(clickPosition: LatLon, horizontalAccuracyInMeters: Double): Set<SplitAtPoint> {
        // ignore first and last node (cannot be split at the very start or end)
        val result = mutableSetOf<SplitAtPoint>()
        for (pos in positions.subList(1, positions.size - 1)) {
            val nodeDistance = distance(clickPosition, pos)
            if (horizontalAccuracyInMeters > nodeDistance) {
                result.add(SplitAtPoint(pos))
            }
        }
        return result
    }

    private fun createSplitsForLines(clickPosition: LatLon, horizontalAccuracyInMeters: Double): Set<SplitAtLinePosition> {
        val result = mutableSetOf<SplitAtLinePosition>()
        positions.forEachPair { first, second ->
            val crossTrackDistance = crossTrackDistance(first, second, clickPosition)
            if (horizontalAccuracyInMeters > crossTrackDistance) {
                val alongTrackDistance = alongTrackDistance(first, second, clickPosition)
                val distance = distance(first, second)
                if (distance > alongTrackDistance && alongTrackDistance > 0) {
                    val delta = alongTrackDistance / distance
                    result.add(SplitAtLinePosition(first, second, delta))
                }
            }
        }
        return result
    }

    @UiThread override fun onClickClose(onConfirmed: Runnable) {
        if (!hasChanges) {
            onDiscard()
            onConfirmed.run()
        } else {
            activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_discard_title)
                    .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                        onDiscard()
                        onConfirmed.run()
                    }
                    .setNegativeButton(R.string.confirmation_discard_negative, null)
                    .show()
            }
        }
    }

    private fun onDiscard() {}

    private fun animateButtonVisibilities() {
        if (isFormComplete) okButton.popIn() else okButton.popOut()
        if (hasChanges) undoButton.popIn() else undoButton.popOut()
    }

    companion object {
        private const val ARG_QUEST_ID = "questId"
        private const val ARG_WAY = "way"
        private const val ARG_ELEMENT_GEOMETRY = "elementGeometry"

        @JvmStatic
        fun create(osmQuestId: Long, way: Way, elementGeometry: ElementGeometry): SplitWayFragment {
            val f = SplitWayFragment()
            f.arguments = bundleOf(
                ARG_QUEST_ID to osmQuestId,
                ARG_WAY to way,
                ARG_ELEMENT_GEOMETRY to elementGeometry
            )
            return f
        }
    }
}
