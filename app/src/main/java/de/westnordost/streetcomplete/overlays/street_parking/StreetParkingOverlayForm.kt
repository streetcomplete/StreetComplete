package de.westnordost.streetcomplete.overlays.street_parking

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.osm.street_parking.DISPLAYED_PARKING_POSITIONS
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingDrawable
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.osm.street_parking.WithFootnoteDrawable
import de.westnordost.streetcomplete.osm.street_parking.applyTo
import de.westnordost.streetcomplete.osm.street_parking.asItem
import de.westnordost.streetcomplete.osm.street_parking.asStreetSideItem
import de.westnordost.streetcomplete.osm.street_parking.createStreetParkingSides
import de.westnordost.streetcomplete.osm.street_parking.validOrNullValues
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.overlays.street_parking.NoParkingSelection.CONDITIONAL_RESTRICTIONS
import de.westnordost.streetcomplete.overlays.street_parking.NoParkingSelection.IMPLICIT
import de.westnordost.streetcomplete.overlays.street_parking.NoParkingSelection.NO_PARKING
import de.westnordost.streetcomplete.overlays.street_parking.NoParkingSelection.NO_STANDING
import de.westnordost.streetcomplete.overlays.street_parking.NoParkingSelection.NO_STOPPING
import de.westnordost.streetcomplete.overlays.street_parking.ParkingSelection.DIAGONAL
import de.westnordost.streetcomplete.overlays.street_parking.ParkingSelection.NO
import de.westnordost.streetcomplete.overlays.street_parking.ParkingSelection.PARALLEL
import de.westnordost.streetcomplete.overlays.street_parking.ParkingSelection.PERPENDICULAR
import de.westnordost.streetcomplete.overlays.street_parking.ParkingSelection.SEPARATE
import de.westnordost.streetcomplete.util.ktx.noParkingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noParkingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStandingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStandingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStoppingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStoppingSignDrawableResId
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item2
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StreetParkingOverlayForm : AStreetSideSelectOverlayForm<StreetParking>() {

    private var currentParking: LeftAndRightStreetParking? = null

    private val isRightSideUpsideDown get() =
        !isForwardOneway && (isReversedOneway || isLeftHandTraffic)

    private val isLeftSideUpsideDown get() =
        !isReversedOneway && (isForwardOneway || isLeftHandTraffic)

    private val isForwardOneway get() = isForwardOneway(element!!.tags)
    private val isReversedOneway get() = isReversedOneway(element!!.tags)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streetSideSelect.defaultPuzzleImageLeft = ResImage(if (isLeftSideUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)
        streetSideSelect.defaultPuzzleImageRight = ResImage(if (isRightSideUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)

        val width = element!!.tags["width"]
        binding.hintTextView.text = if (width != null) {
            val widthFormatted = if (width.toFloatOrNull() != null) width + "m" else width
            getString(R.string.street_parking_street_width, widthFormatted)
        } else null

        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        currentParking = createStreetParkingSides(element!!.tags)?.validOrNullValues()
        streetSideSelect.setPuzzleSide(currentParking?.left?.asStreetSideItem(requireContext(), countryInfo, isUpsideDown(false)), false)
        streetSideSelect.setPuzzleSide(currentParking?.right?.asStreetSideItem(requireContext(), countryInfo, isUpsideDown(true)), true)
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != currentParking?.left ||
        streetSideSelect.right?.value != currentParking?.right

    override fun serialize(item: StreetSideDisplayItem<StreetParking>, isRight: Boolean): String =
        Json.encodeToString(item.value)

    override fun deserialize(str: String, isRight: Boolean): StreetSideDisplayItem<StreetParking> =
        Json.decodeFromString<StreetParking>(str).asStreetSideItem(requireContext(), countryInfo, isUpsideDown(isRight))

    private fun isUpsideDown(isRight: Boolean) =
        if (isRight) isRightSideUpsideDown else isLeftSideUpsideDown

    /* ---------------------------------- selection dialog -------------------------------------- */

    override fun onClickSide(isRight: Boolean) {
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
        streetSideSelect.replacePuzzleSide(parking.asStreetSideItem(ctx, countryInfo, isUpsideDown(isRight)), isRight)
    }

    private fun getParkingItems(context: Context): List<DisplayItem<ParkingSelection>> =
        ParkingSelection.values().map { it.asItem(context, isLeftHandTraffic) }

    private fun getParkingPositionItems(context: Context, orientation: ParkingOrientation) =
        DISPLAYED_PARKING_POSITIONS
            .map { StreetParkingPositionAndOrientation(orientation, it) }
            .map { it.asItem(context, isLeftHandTraffic) }

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        if (streetSideSelect.isComplete) streetSideSelect.saveLastSelection()
        val parking = LeftAndRightStreetParking(streetSideSelect.left?.value, streetSideSelect.right?.value)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        parking.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
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
