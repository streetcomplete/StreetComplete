package de.westnordost.streetcomplete.quests.sidewalk

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.util.normalizeDegrees
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import kotlin.math.absoluteValue

class AddSidewalkForm : AbstractQuestFormAnswerFragment<SidewalkAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sidewalk_separately_mapped) { confirmSeparatelyMappedSidewalk() }
    )

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var leftSide: Sidewalk? = null
    private var rightSide: Sidewalk? = null

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        binding.puzzleView.onClickSideListener = { isRight -> showSidewalkSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_sidewalk_unknown_l
            else                   R.drawable.ic_sidewalk_unknown

        binding.puzzleView.setLeftSideImage(ResImage(leftSide?.iconResId ?: defaultResId))
        binding.puzzleView.setRightSideImage(ResImage(rightSide?.iconResId ?: defaultResId))

        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getString(SIDEWALK_RIGHT)?.let { rightSide = Sidewalk.valueOf(it) }
        savedInstanceState.getString(SIDEWALK_LEFT)?.let { leftSide = Sidewalk.valueOf(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(SIDEWALK_RIGHT, it.name) }
        leftSide?.let { outState.putString(SIDEWALK_LEFT, it.name) }
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun showTapHint() {
        if ((leftSide == null || rightSide == null) && !HAS_SHOWN_TAP_HINT) {
            if (leftSide == null) binding.puzzleView.showLeftSideTapHint()
            if (rightSide == null) binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    private fun showSidewalkSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return

        val items = Sidewalk.values().map { it.asItem() }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) {
            onSelectedSide(it.value!!, isRight)
        }.show()
    }

    private fun onSelectedSide(sidewalk: Sidewalk, isRight: Boolean) {
        if (isRight) {
            binding.puzzleView.replaceRightSideImage(ResImage(sidewalk.iconResId))
            binding.puzzleView.setRightSideText(null)
            rightSide = sidewalk
        } else {
            binding.puzzleView.replaceLeftSideImage(ResImage(sidewalk.iconResId))
            binding.puzzleView.setLeftSideText(null)
            leftSide = sidewalk
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()

        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImageResource(it.left.dialogIconResId)
            binding.lastAnswerButton.rightSideImageView.setImageResource(it.right.dialogIconResId)
        }

        binding.lastAnswerButton.root.setOnClickListener { applyLastSelection() }
    }

    private fun updateLastAnswerButtonVisibility() {
        binding.lastAnswerButton.root.isGone =
            lastSelection == null || leftSide != null || rightSide != null
    }

    private fun saveLastSelection() {
        val leftSide = leftSide
        val rightSide = rightSide
        if (leftSide != null && rightSide != null) {
            lastSelection =
                if (isRoadDisplayedUpsideDown())
                    LastSidewalkSelection(rightSide, leftSide)
                else
                    LastSidewalkSelection(leftSide, rightSide)
        }
    }

    private fun applyLastSelection() {
        val lastSelection = lastSelection ?: return
        if (isRoadDisplayedUpsideDown()) {
            onSelectedSide(lastSelection.right, false)
            onSelectedSide(lastSelection.left, true)
        } else {
            onSelectedSide(lastSelection.left, false)
            onSelectedSide(lastSelection.right, true)
        }
    }

    private fun isRoadDisplayedUpsideDown(): Boolean =
        binding.puzzleView.streetRotation.normalizeDegrees(-180f).absoluteValue > 90f

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        val leftSide = leftSide
        val rightSide = rightSide

        val answer = SidewalkSides(
            left = leftSide == Sidewalk.YES,
            right = rightSide == Sidewalk.YES
        )

        applyAnswer(answer)

        saveLastSelection()
    }

    private fun confirmSeparatelyMappedSidewalk() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(SeparatelyMapped) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = leftSide != null && rightSide != null

    override fun isRejectingClose() = leftSide != null || rightSide != null

    companion object {
        private const val SIDEWALK_LEFT = "sidewalk_left"
        private const val SIDEWALK_RIGHT = "sidewalk_right"

        private var HAS_SHOWN_TAP_HINT = false

        private var lastSelection: LastSidewalkSelection? = null
    }
}

private enum class Sidewalk(val dialogIconResId: Int, val iconResId: Int, val titleResId: Int) {
    NO(R.drawable.ic_sidewalk_no, R.drawable.ic_sidewalk_puzzle_no, R.string.quest_sidewalk_value_no),
    YES(R.drawable.ic_sidewalk_yes, R.drawable.ic_sidewalk_puzzle_yes, R.string.quest_sidewalk_value_yes);

    fun asItem() = Item(this, dialogIconResId, titleResId)
}

private data class LastSidewalkSelection(
    val left: Sidewalk,
    val right: Sidewalk
)
