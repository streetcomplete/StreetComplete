package de.westnordost.streetcomplete.quests.bikeway

import android.os.Bundle
import android.support.annotation.AnyThread
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.TextView

import java.util.Collections

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*


class AddCyclewayForm : AbstractQuestFormAnswerFragment() {

    private val likelyNoBicycleContraflow = FiltersParser().parse("""
            ways with oneway:bicycle != no and
            (oneway ~ yes|-1 and highway ~ primary|secondary|tertiary or junction=roundabout)
        """)

    private var streetSideRotater: StreetSideRotater? = null

    private var isDefiningBothSides: Boolean = false

    private var leftSide: Cycleway? = null
    private var rightSide: Cycleway? = null

    /** returns whether the side that goes into the opposite direction of the driving direction of a
     * one-way is on the right side of the way */
    private val isReverseSideRight get() = isReversedOneway xor isLeftHandTraffic

    private val isOneway get() = isForwardOneway || isReversedOneway

    private val isForwardOneway get() = osmElement.tags["oneway"] == "yes"
    private val isReversedOneway get() = osmElement.tags["oneway"] == "-1"

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.quest_street_side_puzzle)
        setNoContentPadding()

        puzzleView.setListener { this.showCyclewaySelectionDialog(it) }

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedle, elementGeometry)

        initPuzzleDisplay(savedInstanceState)
        initPuzzleImages(savedInstanceState)

        return view
    }

    private fun initPuzzleDisplay(inState: Bundle?) {
        isDefiningBothSides = if (inState != null) {
            inState.getBoolean(DEFINE_BOTH_SIDES)
        } else {
            !likelyNoBicycleContraflow.matches(osmElement)
        }

        if (!isDefiningBothSides) {
            if (isLeftHandTraffic) puzzleView.showOnlyLeftSide()
            else                   puzzleView.showOnlyRightSide()

            if (osmElement.tags["junction"] != "roundabout") {
                addOtherAnswer(R.string.quest_cycleway_answer_contraflow_cycleway) { showBothSides() }
            }
        }
    }

    private fun initPuzzleImages(inState: Bundle?) {
        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_cycleway_unknown_l
            else                   R.drawable.ic_cycleway_unknown

        inState?.getString(CYCLEWAY_RIGHT)?.let {
            rightSide = Cycleway.valueOf(it)
            checkIsFormComplete()
        }
        inState?.getString(CYCLEWAY_LEFT)?.let {
            leftSide = Cycleway.valueOf(it)
            checkIsFormComplete()
        }

        puzzleView.setLeftSideImageResource(leftSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)
        puzzleView.setRightSideImageResource(rightSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(CYCLEWAY_RIGHT, it.name) }
        leftSide?.let { outState.putString(CYCLEWAY_LEFT, it.name) }
        outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides)
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    override fun onClickOk() {
        var isOnewayNotForCyclists = false

        // a cycleway that goes into opposite direction of a oneway street needs special tagging
        val bundle = Bundle()
        val leftSide = leftSide
        val rightSide = rightSide
        if (isOneway && leftSide != null && rightSide != null) {
            // if the road is oneway=-1, a cycleway that goes opposite to it would be cycleway:oneway=yes
            val reverseDir = if (isReversedOneway) 1 else -1

            if (isReverseSideRight) {
                if (rightSide.isSingleTrackOrLane()) {
                    bundle.putInt(CYCLEWAY_RIGHT_DIR, reverseDir)
                }
                isOnewayNotForCyclists = rightSide !== Cycleway.NONE
            } else {
                if (leftSide.isSingleTrackOrLane()) {
                    bundle.putInt(CYCLEWAY_LEFT_DIR, reverseDir)
                }
                isOnewayNotForCyclists = leftSide !== Cycleway.NONE
            }

            isOnewayNotForCyclists = isOnewayNotForCyclists || leftSide.isDualTrackOrLane()
            isOnewayNotForCyclists = isOnewayNotForCyclists || rightSide.isDualTrackOrLane()
        }

        leftSide?.let { bundle.putString(CYCLEWAY_LEFT, it.name) }
        rightSide?.let { bundle.putString(CYCLEWAY_RIGHT, it.name) }
        bundle.putBoolean(IS_ONEWAY_NOT_FOR_CYCLISTS, isOnewayNotForCyclists)
        applyAnswer(bundle)
    }

    private fun Cycleway.isSingleTrackOrLane() =
        this === Cycleway.TRACK || this === Cycleway.EXCLUSIVE_LANE

    private fun Cycleway.isDualTrackOrLane() =
        this === Cycleway.DUAL_TRACK || this === Cycleway.DUAL_LANE

    override fun isFormComplete() =
        if (isDefiningBothSides) leftSide != null && rightSide != null
        else                     leftSide != null || rightSide != null

    private fun showCyclewaySelectionDialog(isRight: Boolean) {
        val recyclerView = RecyclerView(activity)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_select_hint)
            .setView(recyclerView)
            .create()

        recyclerView.adapter = createAdapter(getCyclewayItems(isRight)) { cycleway ->
            alertDialog.dismiss()

            val iconResId = cycleway.getIconResId(isLeftHandTraffic)

            if (isRight) {
                puzzleView.replaceRightSideImageResource(iconResId)
                rightSide = cycleway
            } else {
                puzzleView.replaceLeftSideImageResource(iconResId)
                leftSide = cycleway
            }
            checkIsFormComplete()
        }
        alertDialog.show()
    }

    private fun getCyclewayItems(isRight: Boolean): List<Cycleway> {
        val values = Cycleway.displayValues.toMutableList()
        // different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
        if (isOneway && isReverseSideRight == isRight) {
            Collections.replaceAll(values, Cycleway.PICTOGRAMS, Cycleway.NONE_NO_ONEWAY)
        }
        val country = countryInfo.countryCode
        if ("BE" == country) {
            // Belgium does not make a difference between continuous and dashed lanes -> so don't tag that difference
            // also, in Belgium there is a differentiation between the normal lanes and suggestion lanes
            values.remove(Cycleway.EXCLUSIVE_LANE)
            values.remove(Cycleway.ADVISORY_LANE)
            values.add(0, Cycleway.LANE_UNSPECIFIED)
            values.add(1, Cycleway.SUGGESTION_LANE)
        } else if ("NL" == country) {
            // a differentiation between dashed lanes and suggestion lanes only exist in NL and BE
            values.add(values.indexOf(Cycleway.ADVISORY_LANE) + 1, Cycleway.SUGGESTION_LANE)
        }

        return values
    }

    private fun createAdapter(items: List<Cycleway>, callback: (Cycleway) -> Unit) =
        object : ListAdapter<Cycleway>(items) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : ListAdapter.ViewHolder<Cycleway>(
                    LayoutInflater.from(parent.context).inflate(R.layout.labeled_icon_button_cell, parent, false)
                ) {
                    override fun onBind(with: Cycleway) {
                        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
                        val textView = itemView.findViewById<TextView>(R.id.textView)
                        val resId = with.getIconResId(isLeftHandTraffic)
                        imageView.setImageDrawable(resources.getDrawable(resId))
                        textView.setText(with.nameResId)
                        itemView.setOnClickListener { callback(with) }
                    }
                }
        }

    private fun showBothSides() {
        isDefiningBothSides = true
        puzzleView.showBothSides()
    }

    companion object {
        const val CYCLEWAY_LEFT = "cycleway_left"
        const val CYCLEWAY_RIGHT = "cycleway_right"
        const val CYCLEWAY_LEFT_DIR = "cycleway_left_opposite"
        const val CYCLEWAY_RIGHT_DIR = "cycleway_right_opposite"
        const val IS_ONEWAY_NOT_FOR_CYCLISTS = "oneway_not_for_cyclists"

        private const val DEFINE_BOTH_SIDES = "define_both_sides"
    }
}
