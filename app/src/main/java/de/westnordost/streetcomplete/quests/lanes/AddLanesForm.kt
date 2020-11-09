package de.westnordost.streetcomplete.quests.lanes

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.recyclerview.widget.GridLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.quests.lanes.LanesType.*
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.dialogs.ValuePickerDialog
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_lanes_select_type.view.*
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*
import kotlinx.android.synthetic.main.quest_street_side_puzzle.view.*
import kotlinx.android.synthetic.main.view_little_compass.view.*

class AddLanesForm : AbstractQuestFormAnswerFragment<LanesAnswer>() {

    private var selectedLanesType: LanesType? = null
    private var leftSide: Int? = null
    private var rightSide: Int? = null
    private var isDefiningBothSides: Boolean = false

    private var lastRotation: Float = 0f
    private var lastTilt: Float = 0f

    override val contentPadding get() = selectedLanesType == null

    private var streetSideRotater: StreetSideRotater? = null

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedLanesType = savedInstanceState.getString(LANES_TYPE)?.let { LanesType.valueOf(it) }
            leftSide = savedInstanceState.getInt(LANES_LEFT, -1).takeIf { it != -1 }
            rightSide = savedInstanceState.getInt(LANES_RIGHT, -1).takeIf { it != -1 }
        }

        if (selectedLanesType == null) {
            setSelectLanesTypeLayout()
        } else {
            setStreetSideLayout()
        }
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
        lastRotation = rotation
        lastTilt = tilt
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LANES_TYPE, selectedLanesType?.name)
        leftSide?.let { outState.putInt(LANES_LEFT, it) }
        rightSide?.let { outState.putInt(LANES_RIGHT, it) }
    }

    private fun setSelectLanesTypeLayout() {
        val view = setContentView(R.layout.quest_lanes_select_type)

        val imageSelectAdapter = ImageSelectAdapter<LanesType>(1)
        imageSelectAdapter.cellLayoutId = R.layout.cell_icon_select_with_label_below
        imageSelectAdapter.items = listOf(
            Item(MARKED,       R.drawable.ic_lanes_marked, R.string.quest_streetLanes_answer_lanes),
            Item(UNMARKED,     R.drawable.ic_lanes_unmarked, R.string.quest_streetLanes_answer_noLanes),
            Item(MARKED_SIDES,
                if (isLeftHandTraffic) R.drawable.ic_lanes_marked_odd_l else R.drawable.ic_lanes_marked_odd,
                R.string.quest_streetLanes_answer_lanes_odd
            )
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

    private fun onLanesTypeSelected(lanesType: LanesType) {
        selectedLanesType = lanesType
        setStreetSideLayout()

        if (selectedLanesType != MARKED_SIDES) {
            showSelectLanesDialog(false)
        }
    }

    private fun setStreetSideLayout() {
        val view = setContentView(R.layout.quest_street_side_puzzle)

        view.puzzleView.listener = { isRight -> showSelectLanesDialog(isRight) }

        streetSideRotater = StreetSideRotater(view.puzzleView, view.compassNeedleView, elementGeometry as ElementPolylinesGeometry)
        streetSideRotater?.onMapOrientation(lastRotation, lastTilt)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)

        view.puzzleView.setLeftSideImage(createLeftSideImage())
        view.puzzleView.setRightSideImage(createRightSideImage())

        view.puzzleView.setLeftSideText(if (leftSide != null) null else defaultTitle)
        view.puzzleView.setRightSideText(if (rightSide != null) null else defaultTitle)

        checkIsFormComplete()
    }


    private fun createLeftSideImage() : Image? {
        val leftSide = leftSide ?: return null
        val lanesType = selectedLanesType ?: return null
        return DrawableImage(BitmapDrawable(resources, lanesType.createBitmap(
            256f.toPx(requireContext()).toInt(),
            leftSide, rightSide ?: 0
        )))
    }

    private fun createRightSideImage() : Image? {
        val rightSide = rightSide ?: return null
        val lanesType = selectedLanesType ?: return null
        return DrawableImage(BitmapDrawable(resources, lanesType.createBitmap(
            256f.toPx(requireContext()).toInt(),
            rightSide, leftSide ?: 0
        )))
    }

    private fun showSelectLanesDialog(isRight: Boolean) {
        val currentLaneCount = if (isRight) rightSide else leftSide
        when(selectedLanesType) {
            MARKED -> {
                showSelectMarkedLanesDialog(currentLaneCount) { lanes ->
                    // TODO what about oneways?!
                    check(lanes % 2 == 0)
                    leftSide = lanes / 2
                    rightSide = lanes / 2
                    updatePuzzleView()
                }
            }
            UNMARKED -> {
                showSelectUnmarkedLanesDialog(currentLaneCount) { lanes ->
                    leftSide = lanes / 2
                    rightSide = lanes - lanes / 2
                    updatePuzzleView()
                }
            }
            MARKED_SIDES -> {
                showSelectMarkedLanesDialogForOneSide(currentLaneCount) { lanes ->
                    if (isRight) rightSide = lanes
                    else         leftSide = lanes
                    updatePuzzleView()
                }
            }
        }

    }

    private fun updatePuzzleView() {
        puzzleView.replaceLeftSideImage(createLeftSideImage())
        puzzleView.replaceRightSideImage(createRightSideImage())

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        puzzleView.setLeftSideText(if (leftSide != null) null else defaultTitle)
        puzzleView.setRightSideText(if (rightSide != null) null else defaultTitle)

        checkIsFormComplete()
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
            listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14),
            selectedValue, null,
            R.layout.quest_lanes_select_unmarked_lanes,
            callback).show()
    }

    private fun showSelectMarkedLanesDialogForBothSides(selectedValue: Int?, callback: (value: Int) -> Unit) {
        ValuePickerDialog(requireContext(),
            listOf(2,4,6,8,10,12,14),
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

    companion object {
        private const val LANES_TYPE = "lanes_type"
        private const val LANES_LEFT = "lanes_left"
        private const val LANES_RIGHT = "lanes_right"
    }
}

private enum class LanesType {
    MARKED, MARKED_SIDES, UNMARKED
}

private fun LanesType.createBitmap(height: Int, laneCount: Int, otherLaneCount: Int): Bitmap = when(this) {
    MARKED -> createLanesBitmap(height, laneCount, otherLaneCount, true)
    MARKED_SIDES -> createLanesBitmap(height, laneCount, otherLaneCount, true)
    UNMARKED -> createLanesBitmap(height, laneCount, otherLaneCount, false)
}

// TODO oneways... : MarkedLanesSides nicht für oneways
