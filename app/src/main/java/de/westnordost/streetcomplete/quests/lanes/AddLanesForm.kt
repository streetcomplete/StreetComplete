package de.westnordost.streetcomplete.quests.lanes

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.recyclerview.widget.GridLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.quests.lanes.LanesType.*
import de.westnordost.streetcomplete.view.dialogs.ValuePickerDialog
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_generic_list.view.*
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*
import kotlinx.android.synthetic.main.quest_street_side_puzzle.view.*
import kotlinx.android.synthetic.main.view_little_compass.view.*

class AddLanesForm : AbstractQuestFormAnswerFragment<LanesAnswer>() {

    override val contentLayoutResId = R.layout.quest_generic_list

    private var selectedLanesType: LanesType? = null
    private var leftSide: Int? = null
    private var rightSide: Int? = null
    private var isDefiningBothSides: Boolean = false

    override val contentPadding get() = selectedLanesType != null

    private var streetSideRotater: StreetSideRotater? = null

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSelectLanesTypeLayout()
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun setSelectLanesTypeLayout() {
        val view = setContentView(R.layout.quest_generic_list)

        val imageSelectAdapter = ImageSelectAdapter<LanesType>(1)
        imageSelectAdapter.items = listOf(
            // TODO icons
            Item(MARKED,       R.drawable.fire_hydrant_pillar, R.string.quest_streetLanes_answer_lanes),
            Item(UNMARKED,     R.drawable.fire_hydrant_underground, R.string.quest_streetLanes_answer_noLanes),
            Item(MARKED_SIDES, R.drawable.fire_hydrant_wall, R.string.quest_streetLanes_answer_lanes_odd)
        )
        imageSelectAdapter.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                onLanesTypeSelected(imageSelectAdapter.selectedItems.single())
            }
            override fun onIndexDeselected(index: Int) {}
        })

        view.list.layoutManager = GridLayoutManager(activity, 3)
        view.list.isNestedScrollingEnabled = false
        view.list.adapter = imageSelectAdapter
    }

    private fun setStreetSideLayout() {
        val view = setContentView(R.layout.side_select_puzzle)

        view.puzzleView.listener = { isRight -> showSelectLanesDialog(isRight) }

        streetSideRotater = StreetSideRotater(view.puzzleView, view.compassNeedleView, elementGeometry as ElementPolylinesGeometry)

        val defaultResId = R.drawable.ic_lanes_unknown
        val defaultTitleId = R.string.quest_street_side_puzzle_select

        view.puzzleView.setLeftSideImageResource(leftSide?.puzzleResId ?: defaultResId)
        view.puzzleView.setRightSideImageResource(rightSide?.puzzleResId ?: defaultResId)

        view.puzzleView.setLeftSideText(if (leftSide != null) null else resources.getString( defaultTitleId ))
        view.puzzleView.setRightSideText(if (rightSide != null) null else resources.getString( defaultTitleId ))

        checkIsFormComplete()
    }

    private fun onLanesTypeSelected(lanesType: LanesType) {
        selectedLanesType = lanesType
        setStreetSideLayout()

        if (selectedLanesType != MARKED_SIDES) {
            showSelectLanesDialog(false)
        }
    }

    private fun showSelectLanesDialog(isRight: Boolean) {
        when(selectedLanesType) {
            MARKED -> {
                showSelectMarkedLanesDialog(selectedValue) { lanes ->
                    // TODO what about oneways?!
                    check(lanes % 2 == 0)
                    leftSide = lanes / 2
                    rightSide = lanes / 2
                    puzzleView.replaceLeftSideImageResource(iconResId)
                    puzzleView.replaceRightSideImageResource(iconResId)
                    checkIsFormComplete()
                }
            }
            UNMARKED -> {
                showSelectUnmarkedLanesDialog(selectedValue) { lanes ->
                    leftSide = lanes / 2
                    rightSide = lanes - lanes / 2
                    puzzleView.replaceLeftSideImageResource(iconResId)
                    puzzleView.replaceRightSideImageResource(iconResId)
                    checkIsFormComplete()
                }
            }
            MARKED_SIDES -> {
                showSelectMarkedLanesDialogForOneSide(selectedValue) { lanes ->
                    if (isRight) {
                        puzzleView.replaceRightSideImageResource(iconResId)
                        puzzleView.setRightSideText(lanes.toString())
                        rightSide = lanes
                    } else {
                        puzzleView.replaceLeftSideImageResource(iconResId)
                        puzzleView.setLeftSideText(lanes.toString())
                        leftSide = lanes
                    }
                    checkIsFormComplete()
                }
            }
        }
    }

    private fun showSelectMarkedLanesDialog(selectedValue: Int?, callback: (value: Int) -> Unit) {
        val tags = osmElement!!.tags
        if (tags["oneway"] in listOf("yes","-1") || tags["junction"] == "roundabout") {
            showSelectMarkedLanesDialogForOneSide(selectedValue, callback)
        } else {
            showSelectMarkedLanesDialogForBothSides(selectedValue, callback)
        }
    }

    private fun showSelectUnmarkedLanesDialog(selectedValue: Int?, callback: (value: Int) -> Unit) {
        ValuePickerDialog(
            requireContext(),
            listOf(1,2,3,4,5,6,7,8),
            selectedValue, null,
            R.layout.quest_lanes_select_unmarked_lanes,
            callback).show()
    }

    private fun showSelectMarkedLanesDialogForBothSides(selectedValue: Int?, callback: (value: Int) -> Unit) {
        ValuePickerDialog(requireContext(),
            listOf(1,2,4,6,8,10,12),
            selectedValue, null,
            R.layout.quest_lanes_select_lanes,
            callback).show()
    }

    private fun showSelectMarkedLanesDialogForOneSide(selectedValue: Int?, callback: (value: Int) -> Unit) {
        ValuePickerDialog(requireContext(),
            listOf(1,2,3,4,5,6,7,8),
            selectedValue, null,
            R.layout.quest_lanes_select_lanes_one_side_only,
            callback).show()
    }

    override fun isFormComplete() = selectedLanesType != null &&
        if (isDefiningBothSides) leftSide != null && rightSide != null
        else                     leftSide != null || rightSide != null


    override fun isRejectingClose() = selectedLanesType != null

    override fun onClickOk() {
        val totalLanes = (leftSide ?: 0) + (rightSide ?: 0)
        when(selectedLanesType) {
            MARKED -> applyAnswer(MarkedLanes(totalLanes))
            UNMARKED -> applyAnswer(UnmarkedLanes(totalLanes))
            MARKED_SIDES -> {
                val forwardLanes = if (isLeftHandTraffic) leftSide else rightSide
                val backwardLanes = if (isLeftHandTraffic) rightSide else leftSide
                applyAnswer(MarkedLanesSides(forwardLanes ?: 0, backwardLanes ?: 0))
            }
        }
    }
}

private enum class LanesType {
    MARKED, MARKED_SIDES, UNMARKED
}

// TODO oneways... : MarkedLanesSides nicht für oneways
