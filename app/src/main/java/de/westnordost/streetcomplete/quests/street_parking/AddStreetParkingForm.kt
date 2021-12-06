package de.westnordost.streetcomplete.quests.parking_lanes

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.parking_lanes.ParkingPosition.*
import de.westnordost.streetcomplete.util.normalizeDegrees
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

class AddStreetParkingForm : AbstractQuestFormAnswerFragment<Unit>() {

    // TODO isLeftHandTraffic
    // TODO instance state...?
    // TODO text?
    // TODO actually apply data...

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var leftSide: ParkingPositionAndOrientation? = null
    private var rightSide: ParkingPositionAndOrientation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.puzzleView.onClickSideListener = { isRight -> showParkingOrientationSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        val defaultImg = ResImage(R.drawable.ic_lanes_unknown)

        val ctx = requireContext()
        binding.puzzleView.setLeftSideImage(leftSide?.getImage(ctx) ?: defaultImg)
        binding.puzzleView.setRightSideImage(rightSide?.getImage(ctx) ?: defaultImg)

        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }


    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
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

    private fun showParkingOrientationSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = ParkingOrientation.values()
            .map { Item2(it, ParkingPositionAndOrientation(it, UNKNOWN).getIcon(ctx)) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) {
            showParkingPositionDialog(it.value!!, isRight)
        }.show()
    }

    private fun showParkingPositionDialog(orientation: ParkingOrientation, isRight: Boolean) {
        val ctx = context ?: return
        val items = DISPLAYED_PARKING_POSITIONS
            .map { ParkingPositionAndOrientation(orientation, it) }
            .map { Item2(it, it.getIcon(ctx)) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) {
            onSelectedSide(it.value!!, isRight)
        }.show()
    }

    private fun onSelectedSide(parking: ParkingPositionAndOrientation, isRight: Boolean) {
        val ctx = context ?: return
        val image = parking.getImage(ctx)

        if (isRight) {
            binding.puzzleView.replaceRightSideImage(image)
            rightSide = parking
        } else {
            binding.puzzleView.replaceLeftSideImage(image)
            leftSide = parking
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()
        val ctx = requireContext()
        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImage(it.left.getIcon(ctx))
            binding.lastAnswerButton.rightSideImageView.setImage(it.right.getIcon(ctx))
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
                    LastParkingSelection(rightSide, leftSide)
                else
                    LastParkingSelection(leftSide, rightSide)
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

    override fun isFormComplete(): Boolean = leftSide != null && rightSide != null

    override fun isRejectingClose(): Boolean = leftSide != null || rightSide != null

    override fun onClickOk() {
        // TODO
        applyAnswer(Unit)
        saveLastSelection()
    }

    companion object {
        private var HAS_SHOWN_TAP_HINT = false
        private var lastSelection: LastParkingSelection? = null
    }
}


private data class LastParkingSelection(
    val left: ParkingPositionAndOrientation,
    val right: ParkingPositionAndOrientation
)


data class ParkingPositionAndOrientation(
    val orientation: ParkingOrientation,
    val position: ParkingPosition
)

enum class ParkingOrientation {
    PARALLEL, DIAGONAL, PERPENDICULAR
}

enum class ParkingPosition {
    ON_STREET,
    HALF_ON_KERB,
    ON_KERB,
    STREET_SIDE,
    PAINTED_AREA_ONLY,
    SHOULDER,
    UNKNOWN
}

val DISPLAYED_PARKING_POSITIONS: List<ParkingPosition> = listOf(
    ON_STREET,
    HALF_ON_KERB,
    ON_KERB,
    STREET_SIDE,
    PAINTED_AREA_ONLY,
    SHOULDER
)

fun ParkingPositionAndOrientation.getIcon(context: Context): Image =
    DrawableImage(StreetParkingDrawable(context, this, 128, 128))

fun ParkingPositionAndOrientation.getImage(context: Context): Image =
    DrawableImage(StreetParkingDrawable(context, this, 128, 512))
