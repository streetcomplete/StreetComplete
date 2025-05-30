package de.westnordost.streetcomplete.osm.street_parking

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideItem2
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

/** Functions to display a (parsed) street parking in the UI */

fun StreetParking.asItem(
    context: Context,
    isUpsideDown: Boolean
): DisplayItem<StreetParking> = Item2(
    this,
    getDialogIcon(context, isUpsideDown),
    titleResId?.let { ResText(it) }
)

fun StreetParking.asStreetSideItem(
    context: Context,
    isUpsideDown: Boolean,
    isRightSide: Boolean
): StreetSideDisplayItem<StreetParking> = StreetSideItem2(
    this,
    getIcon(context, isUpsideDown, isRightSide),
    titleResId?.let { ResText(it) },
    getDialogIcon(context, isUpsideDown),
    getFloatingIcon()
)

private val StreetParking.titleResId: Int? get() = when (this) {
    StreetParking.None -> R.string.street_parking_no
    is StreetParking.PositionAndOrientation -> position.titleResId
    StreetParking.Separate -> R.string.street_parking_separate
    StreetParking.Unknown, StreetParking.Incomplete -> null
}

/** Image that should be shown in the street side select puzzle */
private fun StreetParking.getIcon(context: Context, isUpsideDown: Boolean, isRightSide: Boolean): Image = when (this) {
    is StreetParking.PositionAndOrientation ->
        getIcon(context, isUpsideDown, isRightSide)
    StreetParking.None, StreetParking.Separate ->
        ResImage(R.drawable.ic_street_none)
    StreetParking.Unknown, StreetParking.Incomplete ->
        ResImage(if (isUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)
}

/** Icon that should be shown as the icon in a selection dialog */
private fun StreetParking.getDialogIcon(context: Context, isUpsideDown: Boolean): Image = when (this) {
    is StreetParking.PositionAndOrientation ->
        getDialogIcon(context, isUpsideDown)
    StreetParking.None ->
        ResImage(R.drawable.ic_floating_no)
    StreetParking.Separate ->
        ResImage(R.drawable.ic_floating_separate)
    StreetParking.Incomplete, StreetParking.Unknown ->
        ResImage(if (isUpsideDown) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)
}

/** Icon that should be shown as the floating icon in the street side select puzzle */
private fun StreetParking.getFloatingIcon(): Image? = when (this) {
    StreetParking.Separate -> ResImage(R.drawable.ic_floating_separate)
    else -> null
}

fun StreetParking.PositionAndOrientation.asItem(context: Context, isUpsideDown: Boolean) =
    Item2(this, getDialogIcon(context, isUpsideDown), ResText(position.titleResId))

/** An icon for a street parking is square and shows always the same car so it is easier to spot
 *  the variation that matters(on kerb, half on kerb etc) */
private fun StreetParking.PositionAndOrientation.getDialogIcon(context: Context, isUpsideDown: Boolean): Image =
    DrawableImage(StreetParkingDrawable(context, orientation, position, isUpsideDown, 128, 128, R.drawable.ic_car1))

/** An image for a street parking to be displayed shows a wide variety of different cars so that
 *  it looks nicer and/or closer to reality */
private fun StreetParking.PositionAndOrientation.getIcon(context: Context, isUpsideDown: Boolean, isRightSide: Boolean): Image {
    val drawable = StreetParkingDrawable(context, orientation, position, isUpsideDown, 128, 512)
    // show left and right side staggered to each other
    if (isRightSide) drawable.phase = 0.5f
    return DrawableImage(drawable)
}

private val ParkingPosition.titleResId: Int get() = when (this) {
    ON_STREET -> R.string.street_parking_on_street
    HALF_ON_STREET -> R.string.street_parking_half_on_kerb
    OFF_STREET -> R.string.street_parking_on_kerb
    STREET_SIDE -> R.string.street_parking_street_side
    PAINTED_AREA_ONLY, STAGGERED_ON_STREET -> R.string.street_parking_staggered_on_street
    STAGGERED_HALF_ON_STREET -> R.string.street_parking_staggered_half_on_kerb
}

val DISPLAYED_PARKING_POSITIONS: List<ParkingPosition> = listOf(
    ON_STREET,
    HALF_ON_STREET,
    OFF_STREET,
    STREET_SIDE,
    STAGGERED_ON_STREET,
    STAGGERED_HALF_ON_STREET
)
