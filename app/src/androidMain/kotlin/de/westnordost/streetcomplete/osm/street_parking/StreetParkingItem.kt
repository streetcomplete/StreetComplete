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
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.floating_separate
import de.westnordost.streetcomplete.resources.street_parking_half_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_no
import de.westnordost.streetcomplete.resources.street_parking_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_on_street
import de.westnordost.streetcomplete.resources.street_parking_separate
import de.westnordost.streetcomplete.resources.street_parking_staggered_half_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_staggered_on_street
import de.westnordost.streetcomplete.resources.street_parking_street_side
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Functions to display a (parsed) street parking in the UI */

private val StreetParking.title: StringResource? get() = when (this) {
    is StreetParking.PositionAndOrientation ->
        position.title
    StreetParking.None ->
        Res.string.street_parking_no
    StreetParking.Separate ->
        Res.string.street_parking_separate
    StreetParking.Unknown,StreetParking.Incomplete ->
        null
}

/** Image that should be shown in the street side select puzzle */
private fun StreetParking.getIcon(context: Context, isUpsideDown: Boolean, isRightSide: Boolean): Image = when (this) {
    is StreetParking.PositionAndOrientation ->
        getIcon(context, isUpsideDown, isRightSide)
    StreetParking.None, StreetParking.Separate ->
        ResImage(R.drawable.street_none)
    StreetParking.Unknown, StreetParking.Incomplete ->
        ResImage(if (isUpsideDown) R.drawable.street_side_unknown_l else R.drawable.street_side_unknown)
}

/** Icon that should be shown as the icon in a selection dialog */
private fun StreetParking.getDialogIcon(context: Context, isUpsideDown: Boolean): Image = when (this) {
    is StreetParking.PositionAndOrientation ->
        getDialogIcon(context, isUpsideDown)
    StreetParking.None ->
        ResImage(R.drawable.floating_no)
    StreetParking.Separate ->
        ResImage(R.drawable.floating_separate)
    StreetParking.Incomplete, StreetParking.Unknown ->
        ResImage(if (isUpsideDown) R.drawable.street_side_unknown_l else R.drawable.street_side_unknown)
}

/** Icon that should be shown as the floating icon in the street side select puzzle */
private val StreetParking.floatingIcon: DrawableResource? get() = when (this) {
    StreetParking.Separate -> Res.drawable.floating_separate
    else -> null
}

/** An icon for a street parking is square and shows always the same car so it is easier to spot
 *  the variation that matters(on kerb, half on kerb etc) */
private fun StreetParking.PositionAndOrientation.getDialogIcon(context: Context, isUpsideDown: Boolean): Image =
    DrawableImage(StreetParkingDrawable(context, orientation, position, isUpsideDown, 128, 128, R.drawable.car1))

/** An image for a street parking to be displayed shows a wide variety of different cars so that
 *  it looks nicer and/or closer to reality */
private fun StreetParking.PositionAndOrientation.getIcon(context: Context, isUpsideDown: Boolean, isRightSide: Boolean): Image {
    val drawable = StreetParkingDrawable(context, orientation, position, isUpsideDown, 128, 512)
    // show left and right side staggered to each other
    if (isRightSide) drawable.phase = 0.5f
    return DrawableImage(drawable)
}

private val ParkingPosition.title: StringResource get() = when (this) {
    ON_STREET -> Res.string.street_parking_on_street
    HALF_ON_STREET -> Res.string.street_parking_half_on_kerb
    OFF_STREET -> Res.string.street_parking_on_kerb
    STREET_SIDE -> Res.string.street_parking_street_side
    PAINTED_AREA_ONLY, STAGGERED_ON_STREET -> Res.string.street_parking_staggered_on_street
    STAGGERED_HALF_ON_STREET -> Res.string.street_parking_staggered_half_on_kerb
}
