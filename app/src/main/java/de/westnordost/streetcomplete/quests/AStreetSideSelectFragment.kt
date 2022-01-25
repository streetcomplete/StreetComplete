package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.util.normalizeDegrees
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

abstract class AStreetSideSelectFragment<I,T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var leftIndex: Int? = null
    private var rightIndex: Int? = null

    private val left: StreetSideDisplayItem<I>? get() = leftIndex?.let { items[it] }
    private val right: StreetSideDisplayItem<I>? get() = rightIndex?.let { items[it] }

    open val cellLayoutId = R.layout.cell_labeled_image_select

    open val defaultImage get() = ResImage(if (countryInfo.isLeftHandTraffic) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)

    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<StreetSideDisplayItem<I>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        binding.puzzleView.onClickSideListener = { isRight -> showSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        // TODO text

        val left = left
        if (left != null) {
            binding.puzzleView.setLeftSideImage(left.image)
            binding.puzzleView.setLeftSideFloatingIcon(left.floatingIcon)
            binding.puzzleView.setLeftSideText(null)
        } else {
            binding.puzzleView.setLeftSideImage(defaultImage)
        }
        val right = right
        if (right != null) {
            binding.puzzleView.setRightSideImage(right.image)
            binding.puzzleView.setRightSideFloatingIcon(right.floatingIcon)
            binding.puzzleView.setRightSideText(null)
        } else {
            binding.puzzleView.setRightSideImage(defaultImage)
        }

        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        rightIndex = savedInstanceState.getInt(RIGHT_INDEX, -1).takeIf { it != -1 }
        leftIndex = savedInstanceState.getInt(LEFT_INDEX, -1).takeIf { it != -1 }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RIGHT_INDEX, rightIndex ?: -1)
        outState.putInt(LEFT_INDEX, leftIndex ?: -1)
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun showTapHint() {
        if ((leftIndex == null || rightIndex == null) && !HAS_SHOWN_TAP_HINT) {
            if (leftIndex == null) binding.puzzleView.showLeftSideTapHint()
            if (rightIndex == null) binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    private fun showSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return

        ImageListPickerDialog(ctx, items.map { it.toDisplayItem() }, cellLayoutId, 2) { item ->
            onSelectedSide(items.indexOfFirst { it.value == item.value }, isRight)
        }.show()
    }

    private fun onSelectedSide(selectionIndex: Int, isRight: Boolean) {
        val selection = items[selectionIndex]
        if (isRight) {
            binding.puzzleView.replaceRightSideImage(selection.image)
            binding.puzzleView.replaceRightSideFloatingIcon(selection.floatingIcon)
            binding.puzzleView.setRightSideText(null)
            rightIndex = selectionIndex
        } else {
            binding.puzzleView.replaceLeftSideImage(selection.image)
            binding.puzzleView.replaceLeftSideFloatingIcon(selection.floatingIcon)
            binding.puzzleView.setLeftSideText(null)
            leftIndex = selectionIndex
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()

        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImage(items[it.leftIndex].icon)
            binding.lastAnswerButton.rightSideImageView.setImage(items[it.rightIndex].icon)
        }

        binding.lastAnswerButton.root.setOnClickListener { applyLastSelection() }
    }

    private fun updateLastAnswerButtonVisibility() {
        binding.lastAnswerButton.root.isGone =
            lastSelection == null || leftIndex != null || rightIndex != null
    }

    private fun saveLastSelection() {
        val leftIndex = leftIndex
        val rightIndex = rightIndex
        if (leftIndex != null && rightIndex != null) {
            lastSelection =
                if (isRoadDisplayedUpsideDown())
                    LastSelection(rightIndex, leftIndex)
                else
                    LastSelection(leftIndex, rightIndex)
        }
    }

    private fun applyLastSelection() {
        val lastSelection = lastSelection ?: return
        if (isRoadDisplayedUpsideDown()) {
            onSelectedSide(lastSelection.rightIndex, false)
            onSelectedSide(lastSelection.leftIndex, true)
        } else {
            onSelectedSide(lastSelection.leftIndex, false)
            onSelectedSide(lastSelection.rightIndex, true)
        }
    }

    private fun isRoadDisplayedUpsideDown(): Boolean =
        binding.puzzleView.streetRotation.normalizeDegrees(-180f).absoluteValue > 90f

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        onClickOk(left!!.value, right!!.value)
        saveLastSelection()
    }

    abstract fun onClickOk(leftSide: I, rightSide: I)

    override fun isFormComplete() = left != null && right != null

    override fun isRejectingClose() = left != null || right != null

    companion object {
        private const val LEFT_INDEX = "left_index"
        private const val RIGHT_INDEX = "right_index"

        private var HAS_SHOWN_TAP_HINT = false

        private var lastSelection: LastSelection? = null // TODO!!
    }
}

private data class LastSelection(
    val leftIndex: Int,
    val rightIndex: Int
)

interface StreetSideDisplayItem<T> {
    val value: T
    val image: Image?
    val icon: Image?
    val floatingIcon: Image?
    val title: Text?

    fun toDisplayItem(): DisplayItem<T> = Item2(value, icon, title)
}
