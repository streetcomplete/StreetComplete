package de.westnordost.streetcomplete.osm.street_parking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.car1
import de.westnordost.streetcomplete.resources.car1a
import de.westnordost.streetcomplete.resources.car1b
import de.westnordost.streetcomplete.resources.car2
import de.westnordost.streetcomplete.resources.car2a
import de.westnordost.streetcomplete.resources.car2b
import de.westnordost.streetcomplete.resources.car3
import de.westnordost.streetcomplete.resources.car3a
import de.westnordost.streetcomplete.resources.car4
import de.westnordost.streetcomplete.resources.car5
import de.westnordost.streetcomplete.resources.floating_no
import de.westnordost.streetcomplete.resources.floating_separate
import de.westnordost.streetcomplete.resources.street_broad
import de.westnordost.streetcomplete.resources.street_narrow
import de.westnordost.streetcomplete.resources.street_none
import de.westnordost.streetcomplete.resources.street_normal
import de.westnordost.streetcomplete.resources.street_parking_bays_diagonal
import de.westnordost.streetcomplete.resources.street_parking_bays_parallel
import de.westnordost.streetcomplete.resources.street_parking_bays_perpendicular
import de.westnordost.streetcomplete.resources.street_parking_half_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_no
import de.westnordost.streetcomplete.resources.street_parking_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_on_street
import de.westnordost.streetcomplete.resources.street_parking_separate
import de.westnordost.streetcomplete.resources.street_parking_staggered_half_on_kerb
import de.westnordost.streetcomplete.resources.street_parking_staggered_on_street
import de.westnordost.streetcomplete.resources.street_parking_street_side
import de.westnordost.streetcomplete.resources.street_side_unknown
import de.westnordost.streetcomplete.resources.street_side_unknown_l
import de.westnordost.streetcomplete.resources.street_very_narrow
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource

/** Functions to display a (parsed) street parking in the UI */

val StreetParking.title: StringResource? get() = when (this) {
    is StreetParking.PositionAndOrientation ->
        position.title
    StreetParking.None ->
        Res.string.street_parking_no
    StreetParking.Separate ->
        Res.string.street_parking_separate
    StreetParking.Unknown, StreetParking.Incomplete ->
        null
}

/** Image that should be shown in the street side select puzzle */
@Composable
private fun StreetParking.painter(isUpsideDown: Boolean, isRightSide: Boolean): Painter = when (this) {
    is StreetParking.PositionAndOrientation ->
        painter(isUpsideDown, isRightSide)
    StreetParking.None, StreetParking.Separate ->
        painterResource(Res.drawable.street_none)
    StreetParking.Unknown, StreetParking.Incomplete ->
        painterResource(if (isUpsideDown) Res.drawable.street_side_unknown_l else Res.drawable.street_side_unknown)
}

/** Icon that should be shown as the icon in a selection dialog */
@Composable
fun StreetParking.dialogPainter(isUpsideDown: Boolean): Painter = when (this) {
    is StreetParking.PositionAndOrientation ->
        dialogPainter(isUpsideDown)
    StreetParking.None ->
        painterResource(Res.drawable.floating_no)
    StreetParking.Separate ->
        painterResource(Res.drawable.floating_separate)
    StreetParking.Incomplete, StreetParking.Unknown ->
        painterResource(if (isUpsideDown) Res.drawable.street_side_unknown_l else Res.drawable.street_side_unknown)
}

/** Icon that should be shown as the floating icon in the street side select puzzle */
private val StreetParking.floatingIcon: DrawableResource? get() = when (this) {
    StreetParking.Separate -> Res.drawable.floating_separate
    else -> null
}

/** An icon for a street parking is square and shows always the same car so it is easier to spot
 *  the variation that matters(on kerb, half on kerb etc) */
@Composable
private fun StreetParking.PositionAndOrientation.dialogPainter(isUpsideDown: Boolean): Painter {
    val carPainter =
        if (isApril1st()) painterResource(Res.drawable.car_nyan)
        else painterResource(Res.drawable.car1)
    val backgroundPainter = painterResource(background)
    return remember(this, isUpsideDown) {
        StreetParkingPainter(
            intrinsicSize = Size(128f, 128f),
            parkingOrientation = orientation,
            parkingPosition = position,
            backgroundPainter = backgroundPainter,
            carPainters = listOf(carPainter),
            isUpsideDown = isUpsideDown,
        )
    }
}

/** An image for a street parking to be displayed shows a wide variety of different cars so that
 *  it looks nicer and/or closer to reality */
@Composable
private fun StreetParking.PositionAndOrientation.painter(isUpsideDown: Boolean, isRightSide: Boolean): Painter {
    val carPainters =
        if (isApril1st()) listOf(painterResource(Res.drawable.car_nyan))
        else CAR_DRAWABLES.map { painterResource(it) }
    val backgroundPainter = painterResource(background)
    return remember(this, isUpsideDown, isRightSide) {
        StreetParkingPainter(
            intrinsicSize = Size(128f, 512f),
            parkingOrientation = orientation,
            parkingPosition = position,
            backgroundPainter = backgroundPainter,
            carPainters = carPainters,
            isUpsideDown = isUpsideDown,
            // show left and right side staggered to each other
            phase = if (isRightSide) 0.5f else 0f,
        )
    }
}

/** drawables of the street in the background */
private val StreetParking.PositionAndOrientation.background: DrawableResource get() =
    when (position) {
        ON_STREET, PAINTED_AREA_ONLY, STAGGERED_ON_STREET -> when (orientation) {
            PARALLEL -> Res.drawable.street_normal
            else -> Res.drawable.street_broad
        }
        HALF_ON_STREET, STAGGERED_HALF_ON_STREET -> when (orientation) {
            PARALLEL -> Res.drawable.street_narrow
            else -> Res.drawable.street_normal
        }
        OFF_STREET -> Res.drawable.street_very_narrow
        STREET_SIDE -> when (orientation) {
            PARALLEL -> Res.drawable.street_parking_bays_parallel
            DIAGONAL -> Res.drawable.street_parking_bays_diagonal
            PERPENDICULAR -> Res.drawable.street_parking_bays_perpendicular
        }
    }

private val ParkingPosition.title: StringResource get() = when (this) {
    ON_STREET -> Res.string.street_parking_on_street
    HALF_ON_STREET -> Res.string.street_parking_half_on_kerb
    OFF_STREET -> Res.string.street_parking_on_kerb
    STREET_SIDE -> Res.string.street_parking_street_side
    PAINTED_AREA_ONLY, STAGGERED_ON_STREET -> Res.string.street_parking_staggered_on_street
    STAGGERED_HALF_ON_STREET -> Res.string.street_parking_staggered_half_on_kerb
}

private val CAR_DRAWABLES = listOf(
    Res.drawable.car1,
    Res.drawable.car1a,
    Res.drawable.car1b,
    Res.drawable.car2,
    Res.drawable.car2a,
    Res.drawable.car2b,
    Res.drawable.car3,
    Res.drawable.car3a,
    Res.drawable.car4,
    Res.drawable.car5,
)
