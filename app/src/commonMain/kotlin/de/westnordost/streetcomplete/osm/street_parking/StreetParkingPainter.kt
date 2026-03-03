package de.westnordost.streetcomplete.osm.street_parking

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*
import kotlin.math.ceil
import kotlin.random.Random

/** Painter that displays parking cars in the given orientation and position in the given size */
class StreetParkingPainter(
    override val intrinsicSize: Size,
    private val parkingOrientation: ParkingOrientation,
    private val parkingPosition: ParkingPosition?,
    private val carPainters: List<Painter>,
    private val backgroundPainter: Painter?,
    private val isUpsideDown: Boolean,
    private val randomSeed: Int,
    private val phase: Float = 0f,
) : Painter() {
    private val omittedCarIndices = getOmittedCarIndices(parkingOrientation, parkingPosition)

    override fun DrawScope.onDraw() {
        val width = size.width
        val height = size.height
        val startY = (if (phase <= 0f) 0f else phase - 1f) * height
        val random = Random(randomSeed)

        scale(scaleX = 1f, scaleY = if (isUpsideDown) -1f else +1f) {
            // drawing the street background
            if (backgroundPainter != null) {
                val backgroundHeight = backgroundPainter.intrinsicSize.height / backgroundPainter.intrinsicSize.width * width
                var y = startY
                while (y < height) {
                    if (backgroundHeight <= 0f) break
                    translate(top = y) {
                        with(backgroundPainter) { draw(Size(width, backgroundHeight)) }
                    }
                    y += backgroundHeight
                }
            }

            val carWidth = 0.23f * width
            val carX = parkingOrientation.carsX * width - carWidth / 2
            val carRotation = parkingOrientation.carsRotation
            val carCount = parkingOrientation.carCount
            val carSpacingY = (2 * width / carCount)

            // drawing the cars
            var carY = startY
            var i = 0
            while (carY < height) {
                if (i % carCount !in omittedCarIndices) {
                    val carPainter = carPainters[random.nextInt(carPainters.size)]
                    val carIntrinsicSize = carPainter.intrinsicSize
                    val carHeight = carIntrinsicSize.height * carWidth / carIntrinsicSize.width
                    val carPadding = (carSpacingY - carHeight) / 2f
                    translate(left = carX, top = carY + carPadding) {
                        rotate(degrees = carRotation, pivot = Offset(carWidth / 2, carHeight / 2)) {
                            with(carPainter) { draw(Size(carWidth, carHeight)) }
                        }
                    }
                }
                carY += carSpacingY
                i++
            }
        }
    }
}

/** number of cars parked */
private val ParkingOrientation.carCount: Int get() = when (this) {
    PARALLEL -> 4
    DIAGONAL -> 6
    PERPENDICULAR -> 8
}

/** which car indices to not draw */
private fun getOmittedCarIndices(orientation: ParkingOrientation, position: ParkingPosition?): List<Int> =
    when (position) {
        STREET_SIDE -> when (orientation) {
            PARALLEL -> listOf(1, 2)
            DIAGONAL -> listOf(2, 3)
            PERPENDICULAR -> listOf(0, 3, 4, 7)
        }
        PAINTED_AREA_ONLY, STAGGERED_ON_STREET, STAGGERED_HALF_ON_STREET -> when (orientation) {
            PARALLEL -> listOf(0, 3)
            DIAGONAL -> listOf(0, 1, 5)
            PERPENDICULAR -> listOf(0, 1, 6, 7)
        }
        else -> emptyList()
    }


/** center x-position of the cars */
private val ParkingOrientation.carsX: Float get() = when (this) {
    PARALLEL -> 0.44f
    DIAGONAL -> 0.50f
    PERPENDICULAR -> 0.50f
}

/** rotation of the cars */
private val ParkingOrientation.carsRotation: Float get() = when (this) {
    PARALLEL -> 0f
    DIAGONAL -> 55f
    PERPENDICULAR -> 90f
}
