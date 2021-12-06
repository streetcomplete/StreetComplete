package de.westnordost.streetcomplete.quests.parking_lanes

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.parking_lanes.ParkingOrientation.*
import de.westnordost.streetcomplete.quests.parking_lanes.ParkingPosition.*
import kotlin.math.ceil
import kotlin.random.Random

class StreetParkingDrawable(
    private val context: Context,
    private val parking: ParkingPositionAndOrientation,
    private val width: Int,
    private val height: Int
) : Drawable() {

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = (width * context.resources.displayMetrics.density).toInt()
    override fun getIntrinsicHeight(): Int = (height * context.resources.displayMetrics.density).toInt()

    // TODO: different cars
    override fun draw(canvas: Canvas) {
        if (!isVisible) return

        val width = bounds.width()
        val height = width * 2

        val omittedCarIndices = parking.omittedCarIndices
        val carWidth = 0.23f * width
        val carX = parking.x * width - carWidth/2
        val carRotation = parking.rotation
        val carCount = parking.cars
        val backgroundResId = parking.streetDrawableResId

        for (y in 0..(ceil(1.0 * bounds.height() / height ).toInt())) {

            // drawing the street background
            if (backgroundResId != null) {
                val background = context.getDrawable(backgroundResId)!!
                val backgroundHeight = background.intrinsicHeight / background.intrinsicWidth * width
                background.setBounds(0, 0, width, backgroundHeight)
                background.draw(canvas)
            }

            // drawing the cars
            for (i in 0 until carCount) {
                if (i in omittedCarIndices) continue
                val car = context.getDrawable(CAR_RES_IDS[Random.nextInt(CAR_RES_IDS.size)])!!
                val carHeight = car.intrinsicHeight * carWidth / car.intrinsicWidth
                val paddingY = (height / carCount - carHeight) / 2
                val carY = 1f * height / carCount * i + paddingY
                canvas.withSave {
                    canvas.translate(carX, carY)
                    canvas.rotate(carRotation, carWidth / 2, carHeight / 2)
                    car.setBounds(0, 0, carWidth.toInt(), carHeight.toInt())
                    car.draw(canvas)
                }
            }

            canvas.translate(0f, height.toFloat())
        }
    }
}

/** drawable res id of the street in the background */
private val ParkingPositionAndOrientation.streetDrawableResId: Int? get() =
    when(position) {
        ON_STREET -> when(orientation) {
            PARALLEL -> R.drawable.ic_street
            else -> R.drawable.ic_street_broad
        }
        HALF_ON_KERB -> when(orientation) {
            PARALLEL -> R.drawable.ic_street_narrow
            else -> R.drawable.ic_street
        }
        ON_KERB -> R.drawable.ic_street_very_narrow
        STREET_SIDE -> when(orientation) {
            PARALLEL -> R.drawable.ic_street_parking_bays_parallel
            DIAGONAL -> R.drawable.ic_street_parking_bays_diagonal
            PERPENDICULAR -> R.drawable.ic_street_parking_bays_perpendicular
        }
        PAINTED_AREA_ONLY -> when(orientation) {
            PARALLEL -> R.drawable.ic_street_marked_parking_parallel
            DIAGONAL -> R.drawable.ic_street_marked_parking_diagonal
            PERPENDICULAR -> R.drawable.ic_street_marked_parking_perpendicular
        }
        SHOULDER -> when(orientation) {
            PARALLEL -> R.drawable.ic_street_shoulder
            else -> R.drawable.ic_street_shoulder_broad
        }
        else -> null
    }

/** number of cars parked */
private val ParkingPositionAndOrientation.cars: Int get() = when(orientation) {
    PARALLEL -> 4
    DIAGONAL -> 6
    PERPENDICULAR -> 8
}

/** which car indices to not draw */
private val ParkingPositionAndOrientation.omittedCarIndices: List<Int> get() =
    when(position) {
        STREET_SIDE -> when(orientation) {
            PARALLEL -> listOf(1,2)
            DIAGONAL -> listOf(2,3)
            PERPENDICULAR -> listOf(0,3,4,7)
        }
        PAINTED_AREA_ONLY -> when(orientation) {
            PARALLEL -> listOf(0,3)
            DIAGONAL -> listOf(0,1,4,5)
            PERPENDICULAR -> listOf(0,1,5,6,7)
        }
        else -> emptyList()
    }


/** center x-position of the cars */
private val ParkingPositionAndOrientation.x: Float get() = when (orientation) {
    PARALLEL -> 0.44f
    DIAGONAL -> 0.49f
    PERPENDICULAR -> 0.50f
}

/** rotation of the cars */
private val ParkingPositionAndOrientation.rotation: Float get() = when(orientation) {
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
    R.drawable.ic_car3,
    R.drawable.ic_car3a,
    R.drawable.ic_car4,
    R.drawable.ic_car5,
)
