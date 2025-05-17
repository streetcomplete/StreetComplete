package de.westnordost.streetcomplete.osm.street_parking

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.graphics.withSave
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

/** Drawable that displays parking cars in the given orientation and position in the given size */
class StreetParkingDrawable(
    private val context: Context,
    private val parkingOrientation: ParkingOrientation,
    private val parkingPosition: ParkingPosition?,
    private val isUpsideDown: Boolean,
    private val width: Int = 128,
    private val height: Int = 128,
    @DrawableRes private val staticCarDrawableResId: Int? = null
) : Drawable() {

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth() = context.resources.dpToPx(width).roundToInt()
    override fun getIntrinsicHeight() = context.resources.dpToPx(height).roundToInt()

    /** Y-Offset in percent (0..<1) */
    var phase: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return

        if (isUpsideDown) {
            val pivotY = bounds.height() / ceil(height / width / 2f)
            canvas.scale(1f, -1f, bounds.width() / 2f, pivotY)
        }

        val height = bounds.height().toFloat() / (height / width) * 2f
        val width = bounds.width()

        val omittedCarIndices = getOmittedCarIndices(parkingOrientation, parkingPosition)
        val carWidth = 0.23f * width
        val carX = parkingOrientation.carsX * width - carWidth / 2
        val carRotation = parkingOrientation.carsRotation
        val carCount = parkingOrientation.carCount
        val backgroundResId = getStreetDrawableResId(parkingOrientation, parkingPosition)
        val nyanResId = if (isApril1st()) R.drawable.car_nyan else null
        val repeats = ceil(bounds.height() / height).toInt()

        // drawing the street background
        if (backgroundResId != null) {
            val background = context.getDrawable(backgroundResId)!!
            val backgroundHeight =
                (background.intrinsicHeight.toDouble() / background.intrinsicWidth * width).toInt()
            val offsetY = (phase * height).toInt()
            for (i in 0 until repeats) {
                val y = (i * height).toInt() + offsetY
                background.setBounds(0, y, width, backgroundHeight + y)
                background.draw(canvas)
            }
            if (offsetY != 0) {
                background.setBounds(0, -backgroundHeight + offsetY, width, offsetY)
                background.draw(canvas)
            }
        }

        // drawing the cars
        for (i in 0 until carCount * repeats) {
            if (i % carCount in omittedCarIndices) continue
            val carResId =
                nyanResId ?: staticCarDrawableResId ?: CAR_RES_IDS[Random.nextInt(CAR_RES_IDS.size)]
            val car = context.getDrawable(carResId)!!
            val carHeight = car.intrinsicHeight * carWidth / car.intrinsicWidth
            val paddingY = (height / carCount - carHeight) / 2
            val carY = (1f * height / carCount * i + paddingY + phase * height) % (height * repeats)
            canvas.withSave {
                canvas.translate(carX, carY)
                canvas.rotate(carRotation, carWidth / 2, carHeight / 2)
                car.setBounds(0, 0, carWidth.toInt(), carHeight.toInt())
                car.draw(canvas)
            }
        }
    }
}

/** drawable res id of the street in the background */
private fun getStreetDrawableResId(orientation: ParkingOrientation, position: ParkingPosition?): Int? =
    when (position) {
        ON_STREET, PAINTED_AREA_ONLY, STAGGERED_ON_STREET -> when (orientation) {
            PARALLEL -> R.drawable.ic_street
            else -> R.drawable.ic_street_broad
        }
        HALF_ON_STREET, STAGGERED_HALF_ON_STREET -> when (orientation) {
            PARALLEL -> R.drawable.ic_street_narrow
            else -> R.drawable.ic_street
        }
        OFF_STREET -> R.drawable.ic_street_very_narrow
        STREET_SIDE -> when (orientation) {
            PARALLEL -> R.drawable.ic_street_parking_bays_parallel
            DIAGONAL -> R.drawable.ic_street_parking_bays_diagonal
            PERPENDICULAR -> R.drawable.ic_street_parking_bays_perpendicular
        }
        null -> null
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

private val CAR_RES_IDS = listOf(
    R.drawable.ic_car1,
    R.drawable.ic_car1a,
    R.drawable.ic_car1b,
    R.drawable.ic_car2,
    R.drawable.ic_car2a,
    R.drawable.ic_car2b,
    R.drawable.ic_car3,
    R.drawable.ic_car3a,
    R.drawable.ic_car4,
    R.drawable.ic_car5,
)
