package de.westnordost.streetcomplete.quests.street_parking

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.street_parking.NoParkingSelection.CONDITIONAL_RESTRICTIONS
import de.westnordost.streetcomplete.quests.street_parking.NoParkingSelection.IMPLICIT
import de.westnordost.streetcomplete.quests.street_parking.NoParkingSelection.NO_PARKING
import de.westnordost.streetcomplete.quests.street_parking.NoParkingSelection.NO_STANDING
import de.westnordost.streetcomplete.quests.street_parking.NoParkingSelection.NO_STOPPING
import de.westnordost.streetcomplete.quests.street_parking.ParkingSelection.DIAGONAL
import de.westnordost.streetcomplete.quests.street_parking.ParkingSelection.NO
import de.westnordost.streetcomplete.quests.street_parking.ParkingSelection.PARALLEL
import de.westnordost.streetcomplete.quests.street_parking.ParkingSelection.PERPENDICULAR
import de.westnordost.streetcomplete.quests.street_parking.ParkingSelection.SEPARATE
import de.westnordost.streetcomplete.util.ktx.noParkingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noParkingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStandingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStandingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStoppingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStoppingSignDrawableResId
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

class AddStreetParkingForm : AbstractQuestFormAnswerFragment<LeftAndRightStreetParking>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var leftSide: StreetParking? = null
    private var rightSide: StreetParking? = null

    private val isRightSideUpsideDown get() =
        !isForwardOneway && (isReversedOneway || isLeftHandTraffic)

    private val isLeftSideUpsideDown get() =
        !isReversedOneway && (isForwardOneway || isLeftHandTraffic)

    private val isForwardOneway get() = isForwardOneway(osmElement!!.tags)
    private val isReversedOneway get() = isReversedOneway(osmElement!!.tags)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.puzzleView.onClickSideListener = { isRight -> showParkingSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        val defaultLeftImg = ResImage(if (isLeftSideUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)
        val defaultRightImg = ResImage(if (isRightSideUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)

        val ctx = requireContext()
        binding.puzzleView.setLeftSideImage(leftSide?.getIcon(ctx, countryInfo, isLeftSideUpsideDown) ?: defaultLeftImg)
        binding.puzzleView.setLeftSideFloatingIcon(leftSide?.getFloatingIcon(countryInfo))
        binding.puzzleView.setRightSideImage(rightSide?.getIcon(ctx, countryInfo, isRightSideUpsideDown) ?: defaultRightImg)
        binding.puzzleView.setRightSideFloatingIcon(rightSide?.getFloatingIcon(countryInfo))

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

    private fun showParkingSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = getParkingItems(ctx)
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 2, R.string.select_street_parking_orientation) {
            when (it.value!!) {
                NO -> showNoParkingSelectionDialog(isRight)
                SEPARATE -> onSelectedSide(StreetParkingSeparate, isRight)
                PARALLEL -> showParkingPositionDialog(ParkingOrientation.PARALLEL, isRight)
                DIAGONAL -> showParkingPositionDialog(ParkingOrientation.DIAGONAL, isRight)
                PERPENDICULAR -> showParkingPositionDialog(ParkingOrientation.PERPENDICULAR, isRight)
            }
        }.show()
    }

    private fun showNoParkingSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = getNoParkingSelectionItems(ctx, countryInfo)
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 2, R.string.select_street_parking_no) {
            when (it.value!!) {
                NO_STOPPING -> onSelectedSide(StreetStoppingProhibited, isRight)
                NO_STANDING -> onSelectedSide(StreetStandingProhibited, isRight)
                NO_PARKING -> onSelectedSide(StreetParkingProhibited, isRight)
                CONDITIONAL_RESTRICTIONS -> showConditionalRestrictionsHint()
                IMPLICIT -> onSelectedSide(NoStreetParking, isRight)
            }
        }.show()
    }

    private fun showParkingPositionDialog(orientation: ParkingOrientation, isRight: Boolean) {
        val ctx = context ?: return
        val items = getParkingPositionItems(ctx, orientation)
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2, R.string.select_street_parking_position) {
            onSelectedSide(it.value!!, isRight)
        }.show()
    }

    private fun showConditionalRestrictionsHint() {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.street_parking_conditional_restrictions_hint)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun onSelectedSide(parking: StreetParking, isRight: Boolean) {
        val ctx = context ?: return

        val floatingIcon = parking.getFloatingIcon(countryInfo)
        if (isRight) {
            binding.puzzleView.replaceRightSideImage(parking.getIcon(ctx, countryInfo, isRightSideUpsideDown))
            binding.puzzleView.replaceRightSideFloatingIcon(floatingIcon)
            rightSide = parking
        } else {
            binding.puzzleView.replaceLeftSideImage(parking.getIcon(ctx, countryInfo, isLeftSideUpsideDown))
            binding.puzzleView.replaceLeftSideFloatingIcon(floatingIcon)
            leftSide = parking
        }

        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    private fun getParkingItems(context: Context): List<DisplayItem<ParkingSelection>> =
        ParkingSelection.values().map { it.asItem(context, isLeftHandTraffic) }

    private fun getParkingPositionItems(context: Context, orientation: ParkingOrientation) =
        DISPLAYED_PARKING_POSITIONS
            .map { StreetParkingPositionAndOrientation(orientation, it) }
            .map { it.asItem(context, isLeftHandTraffic) }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()
        val ctx = requireContext()
        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImage(it.left!!.getDialogIcon(ctx, countryInfo, isLeftSideUpsideDown))
            binding.lastAnswerButton.rightSideImageView.setImage(it.right!!.getDialogIcon(ctx, countryInfo, isRightSideUpsideDown))
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
            lastSelection = if (isRoadDisplayedUpsideDown())
                LeftAndRightStreetParking(rightSide, leftSide)
            else
                LeftAndRightStreetParking(leftSide, rightSide)
        }
    }

    private fun applyLastSelection() {
        val lastSelection = lastSelection ?: return
        if (isRoadDisplayedUpsideDown()) {
            onSelectedSide(lastSelection.right!!, false)
            onSelectedSide(lastSelection.left!!, true)
        } else {
            onSelectedSide(lastSelection.left!!, false)
            onSelectedSide(lastSelection.right!!, true)
        }
    }

    private fun isRoadDisplayedUpsideDown(): Boolean =
        normalizeDegrees(binding.puzzleView.streetRotation, -180f).absoluteValue > 90f

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun isFormComplete(): Boolean = leftSide != null && rightSide != null

    override fun isRejectingClose(): Boolean = leftSide != null || rightSide != null

    override fun onClickOk() {
        applyAnswer(LeftAndRightStreetParking(leftSide, rightSide))
        saveLastSelection()
    }

    companion object {
        private var HAS_SHOWN_TAP_HINT = false
        private var lastSelection: LeftAndRightStreetParking? = null
    }
}

private enum class ParkingSelection {
    PARALLEL, DIAGONAL, PERPENDICULAR, SEPARATE, NO
}

private val ParkingSelection.titleResId: Int get() = when (this) {
    PARALLEL -> R.string.street_parking_parallel
    DIAGONAL -> R.string.street_parking_diagonal
    PERPENDICULAR -> R.string.street_parking_perpendicular
    SEPARATE -> R.string.street_parking_separate
    NO -> R.string.street_parking_no
}

private fun ParkingSelection.getDialogIcon(context: Context, isUpsideDown: Boolean): Image = when (this) {
    PARALLEL -> createParkingOrientationImage(context, isUpsideDown, ParkingOrientation.PARALLEL)
    DIAGONAL -> createParkingOrientationImage(context, isUpsideDown, ParkingOrientation.DIAGONAL)
    PERPENDICULAR -> createParkingOrientationImage(context, isUpsideDown, ParkingOrientation.PERPENDICULAR)
    SEPARATE -> ResImage(R.drawable.ic_parking_separate)
    NO -> ResImage(R.drawable.ic_parking_no)
}

private fun ParkingSelection.asItem(context: Context, isUpsideDown: Boolean) =
    Item2(this, getDialogIcon(context, isUpsideDown), ResText(titleResId))

private fun createParkingOrientationImage(
    context: Context,
    isUpsideDown: Boolean,
    parkingOrientation: ParkingOrientation,
) =
    DrawableImage(StreetParkingDrawable(context, parkingOrientation, null, isUpsideDown, 128, 128, R.drawable.ic_car1))

private enum class NoParkingSelection {
    NO_STOPPING, NO_STANDING, NO_PARKING, CONDITIONAL_RESTRICTIONS, IMPLICIT
}

private fun getNoParkingSelectionItems(context: Context, countryInfo: CountryInfo) = listOfNotNull(
    // no stopping sign
    Item2(NO_STOPPING, ResImage(countryInfo.noStoppingSignDrawableResId), ResText(R.string.street_stopping_prohibited)),
    // no stopping road marking (if any)
    countryInfo.noStoppingLineStyleResId?.let { resId ->
        Item2(NO_STOPPING, ResImage(resId), ResText(R.string.street_stopping_prohibited))
    },
    // no standing sign (if any)
    countryInfo.noStandingSignDrawableResId?.let { resId ->
        Item2(NO_STANDING, ResImage(resId), ResText(R.string.street_standing_prohibited))
    },
    // no standing road marking (if any)
    countryInfo.noStandingLineStyleResId?.let { resId ->
        Item2(NO_STANDING, ResImage(resId), ResText(R.string.street_standing_prohibited))
    },
    // no parking sign
    Item2(NO_PARKING, ResImage(countryInfo.noParkingSignDrawableResId), ResText(R.string.street_parking_prohibited)),
    // no parking road marking (if any)
    countryInfo.noParkingLineStyleResId?.let { resId ->
        Item2(NO_PARKING, ResImage(resId), ResText(R.string.street_parking_prohibited))
    },
    // conditional restrictions sign
    Item2(
        CONDITIONAL_RESTRICTIONS,
        DrawableImage(WithFootnoteDrawable(context, context.getDrawable(countryInfo.noParkingSignDrawableResId)!!)),
        ResText(R.string.street_parking_conditional_restrictions)
    ),
    // finally, "other reasons" item
    Item2(IMPLICIT, ResImage(R.drawable.ic_parking_no), ResText(R.string.street_parking_no_other_reasons))
)
