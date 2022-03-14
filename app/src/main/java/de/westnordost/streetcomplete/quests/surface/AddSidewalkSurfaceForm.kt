package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.sidewalk.imageResId
import de.westnordost.streetcomplete.quests.sidewalk.titleResId
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import kotlin.math.absoluteValue

class AddSidewalkSurfaceForm : AbstractQuestFormAnswerFragment<SidewalkSurfaceAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    private val currentSidewalks get() = createSidewalkSides(osmElement!!.tags)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var isDefiningBothSides: Boolean = false
    private var isLeftSideNotDefined: Boolean = false
    private var isRightSideNotDefined: Boolean = false

    private var leftSide: SurfaceAnswer? = null
    private var rightSide: SurfaceAnswer? = null

    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.puzzleView.onClickSideListener = { isRight -> showSurfaceSelectionDialog(isRight) }

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_street_side_unknown_l
            else                   R.drawable.ic_street_side_unknown

        binding.puzzleView.setLeftSideImage(ResImage(defaultResId))
        binding.puzzleView.setRightSideImage(ResImage(defaultResId))

        initStateFromTags()

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }

    private fun initStateFromTags() {
        val left = currentSidewalks?.left
        val right = currentSidewalks?.right

        if (left != null && right != null) {
            isDefiningBothSides = (left == Sidewalk.YES) && (right == Sidewalk.YES)
            isRightSideNotDefined = (right == Sidewalk.NO) || (right == Sidewalk.SEPARATE)
            isLeftSideNotDefined = (left == Sidewalk.NO) || (left == Sidewalk.SEPARATE)
            if (right == Sidewalk.NO || right == Sidewalk.SEPARATE) {
                binding.puzzleView.setRightSideText(ResText(right.titleResId))
                binding.puzzleView.setRightSideImage(ResImage(right.imageResId))
                binding.puzzleView.onlyLeftSideClickable()
            }
            if (left == Sidewalk.NO || left == Sidewalk.SEPARATE) {
                binding.puzzleView.setLeftSideText(ResText(left.titleResId))
                binding.puzzleView.setLeftSideImage(ResImage(left.imageResId))
                binding.puzzleView.onlyRightSideClickable()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(SIDEWALK_SURFACE_RIGHT, it.value.name) }
        leftSide?.let { outState.putString(SIDEWALK_SURFACE_LEFT, it.value.name) }
        outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides)
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun showTapHint() {
        if ((leftSide == null || rightSide == null) && !HAS_SHOWN_TAP_HINT) {
            if (leftSide == null && !isLeftSideNotDefined) binding.puzzleView.showLeftSideTapHint()
            if (rightSide == null && !isRightSideNotDefined) binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }
    }

    private fun showBothSides() {
        isDefiningBothSides = true
        binding.puzzleView.showBothSides()
        binding.puzzleView.bothSidesClickable()
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    private fun showSurfaceSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = (PAVED_SURFACES + UNPAVED_SURFACES + Surface.WOODCHIPS + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()
        ImageListPickerDialog(ctx, items, R.layout.cell_labeled_image_select, 2) {
            if (it.value!!.shouldBeDescribed) {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        DescribeGenericSurfaceDialog(requireContext()) { description ->
                            onSelectedSide(SurfaceAnswer(it.value!!, description), isRight)
                        }.show()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                onSelectedSide(SurfaceAnswer(it.value!!), isRight)
            }
        }.show()
    }

    private fun onSelectedSide(surface: SurfaceAnswer, isRight: Boolean) {
        val image = DrawableImage(RotatedCircleDrawable(resources.getDrawable(surface.value.asItem().drawableId!!)))

        if (isRight) {
            binding.puzzleView.replaceRightSideFloatingIcon(image)
            binding.puzzleView.replaceRightSideImage(ResImage(R.drawable.ic_sidewalk_illustration_yes))
            rightSide = surface
        } else {
            binding.puzzleView.replaceLeftSideFloatingIcon(image)
            binding.puzzleView.replaceLeftSideImage(ResImage(R.drawable.ic_sidewalk_illustration_yes))
            leftSide = surface
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()

        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImageResource(it.left.value.asItem().drawableId!!)
            binding.lastAnswerButton.rightSideImageView.setImageResource(it.right.value.asItem().drawableId!!)
        }

        binding.lastAnswerButton.root.setOnClickListener { applyLastSelection() }
    }

    private fun updateLastAnswerButtonVisibility() {
        val formIsPrefilled = leftSide != null || rightSide != null
        val lastAnswerWasForBothSides = (lastSelection?.left != null && lastSelection?.right != null)
        val isDefiningBothSides = isDefiningBothSides && lastAnswerWasForBothSides

        binding.lastAnswerButton.root.isGone =
            lastSelection == null || formIsPrefilled || !isDefiningBothSides
    }

    private fun saveLastSelection() {
        val leftSide = leftSide
        val rightSide = rightSide
        if (leftSide != null && rightSide != null) {
            lastSelection =
                if (isRoadDisplayedUpsideDown())
                    LastSidewalkSurfaceSelection(rightSide, leftSide)
                else
                    LastSidewalkSurfaceSelection(leftSide, rightSide)
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
        normalizeDegrees(binding.puzzleView.streetRotation, -180f).absoluteValue > 90f

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        val leftSide = leftSide
        val rightSide = rightSide

        val answer = SidewalkSurfaceAnswer(
            left = leftSide,
            right = rightSide
        )

        applyAnswer(answer)

        saveLastSelection()
    }

    override fun isFormComplete() = (
        if (isDefiningBothSides) leftSide != null && rightSide != null
        else                     leftSide != null || rightSide != null
    )

    override fun isRejectingClose() = (leftSide != null || rightSide != null)

    companion object {
        private const val SIDEWALK_SURFACE_LEFT = "sidewalk_surface_left"
        private const val SIDEWALK_SURFACE_RIGHT = "sidewalk_surface_right"
        private const val DEFINE_BOTH_SIDES = "define_both_sides"

        private var HAS_SHOWN_TAP_HINT = false

        private var lastSelection: LastSidewalkSurfaceSelection? = null
    }
}

private data class LastSidewalkSurfaceSelection(
    val left: SurfaceAnswer,
    val right: SurfaceAnswer
)
