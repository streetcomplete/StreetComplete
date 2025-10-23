package de.westnordost.streetcomplete.osm.street_parking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSelection.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.car1
import de.westnordost.streetcomplete.resources.car_nyan
import de.westnordost.streetcomplete.resources.floating_no
import de.westnordost.streetcomplete.resources.floating_separate
import de.westnordost.streetcomplete.resources.street_parking_diagonal
import de.westnordost.streetcomplete.resources.street_parking_no
import de.westnordost.streetcomplete.resources.street_parking_parallel
import de.westnordost.streetcomplete.resources.street_parking_perpendicular
import de.westnordost.streetcomplete.resources.street_parking_separate
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

val StreetParkingSelection.title: StringResource get() = when (this) {
    PARALLEL -> Res.string.street_parking_parallel
    DIAGONAL -> Res.string.street_parking_diagonal
    PERPENDICULAR -> Res.string.street_parking_perpendicular
    SEPARATE -> Res.string.street_parking_separate
    NO -> Res.string.street_parking_no
}

@Composable
fun StreetParkingSelection.painter(isUpsideDown: Boolean): Painter = when (this) {
    PARALLEL -> parkingOrientationPainter(isUpsideDown, ParkingOrientation.PARALLEL)
    DIAGONAL -> parkingOrientationPainter(isUpsideDown, ParkingOrientation.DIAGONAL)
    PERPENDICULAR -> parkingOrientationPainter(isUpsideDown, ParkingOrientation.PERPENDICULAR)
    SEPARATE -> painterResource(Res.drawable.floating_separate)
    NO -> painterResource(Res.drawable.floating_no)
}

@Composable
private fun parkingOrientationPainter(
    isUpsideDown: Boolean,
    parkingOrientation: ParkingOrientation,
): Painter {
    val carPainter =
        if (isApril1st()) painterResource(Res.drawable.car_nyan)
        else painterResource(Res.drawable.car1)
    val random = remember { Random(Random.Default.nextInt()) }
    return remember(parkingOrientation, isUpsideDown) {
        StreetParkingPainter(
            intrinsicSize = Size(128f, 128f),
            parkingOrientation = parkingOrientation,
            parkingPosition = null,
            carPainters = listOf(carPainter),
            backgroundPainter = null,
            isUpsideDown = isUpsideDown,
            random = random,
        )
    }
}
