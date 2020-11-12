package de.westnordost.streetcomplete.quests.lanes

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.StreetRotateable
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle
import kotlin.math.max
import kotlin.random.Random

class LanesSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), StreetRotateable, LifecycleObserver {

    private val puzzle = StreetSideSelectPuzzle(context, attrs, defStyleAttr)

    private val animator = TimeAnimator()

    var onClickSideListener: ((isRight: Boolean) -> Unit)?
        set(value) { puzzle.onClickSideListener = value }
        get() = puzzle.onClickSideListener

    var onClickListener: (() -> Unit)?
        set(value) { puzzle.onClickListener = value }
        get() = puzzle.onClickListener

    var isShowingLaneMarkings: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            updateLanesShowingSides()
            doOnLayout { updateLanes() }
        }
    }
    var laneCountLeft: Int? = null
    set(value) {
        if (field != value) {
            field = value
            updateCarViewsOnLanes(value ?: 0, carsOnLanesLeft)
            doOnLayout { updateLanes() }
        }
    }
    var laneCountRight: Int? = null
    set(value) {
        if (field != value) {
            field = value
            updateCarViewsOnLanes(value ?: 0, carsOnLanesRight)
            doOnLayout { updateLanes() }
        }
    }

    var isShowingBothSides: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            updateLanesShowingSides()
        }
    }

    var isForwardTraffic: Boolean = true

    private val carsOnLanesLeft = mutableListOf<CarViewAndPosition>()
    private val carsOnLanesRight = mutableListOf<CarViewAndPosition>()

    private val roadWidth get() = puzzle.streetSidesView.width
    private val roadHeight get() = puzzle.streetSidesView.height

    private val roadLaneWidth: Int get() {
        val w = roadWidth
        val h = roadHeight
        if (w == 0 || h == 0) return 0
        val maxLaneCount = max(laneCountLeft ?: 0, laneCountRight ?: 0)
        val totalLaneCount = (laneCountLeft ?: 0) + (laneCountRight ?: 0)
        if (maxLaneCount == 0) return 0

        return if (!isShowingBothSides || !isShowingLaneMarkings)
            w / totalLaneCount
        else
            w / 2 / maxLaneCount
    }

    init {
        addView(puzzle, MATCH_PARENT, MATCH_PARENT)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        puzzle.setLeftSideText(defaultTitle)
        puzzle.setRightSideText(defaultTitle)

        animator.setTimeListener { _, _, deltaTime ->
            moveCars(deltaTime)
            renderCars()
        }
        //animator.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) fun resume() {
        animator.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE) fun pause() {
        animator.end()
    }

    override fun setStreetRotation(rotation: Float) {
        puzzle.setStreetRotation(rotation)
    }

    //region lanes

    private fun updateLanesShowingSides() {
        if (!isShowingBothSides || !isShowingLaneMarkings) {
            puzzle.showOnlyRightSide()
            laneCountLeft = null
        } else {
            puzzle.showBothSides()
        }
    }

    /** update the lanes on the street */
    private fun updateLanes() {
        if (height == 0) return
        val leftBitmap: Bitmap?
        val rightBitmap: Bitmap?
        if (isShowingLaneMarkings) {
            leftBitmap = laneCountLeft?.let { createLanesBitmap(height, it, laneCountRight, true) }
            rightBitmap = laneCountRight?.let { createLanesBitmap(height, it, laneCountLeft, true) }
        } else {
            val totalLaneCount = ((laneCountLeft ?: 0) + (laneCountRight ?: 0)).takeIf { it > 0 }
            leftBitmap = null
            rightBitmap = totalLaneCount?.let { createLanesBitmap(height, it, null, false) }
        }

        puzzle.setLeftSideImage(leftBitmap?.asImage())
        puzzle.setRightSideImage(rightBitmap?.asImage())

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        puzzle.setLeftSideText(if (laneCountLeft != null) null else defaultTitle)
        puzzle.setRightSideText(if (laneCountRight != null) null else defaultTitle)

        renderCars()
    }

    private fun Bitmap.asImage() = DrawableImage(BitmapDrawable(resources, this))

    // endregion

    //region cars

    /** initialize/delete car views given the new lane count */
    private fun updateCarViewsOnLanes(laneCount: Int, carsOnLanes: MutableList<CarViewAndPosition>) {
        while (carsOnLanes.size < laneCount) {
            val view = ImageView(context)
            view.scaleType = ImageView.ScaleType.CENTER_INSIDE
            view.setImageResource(getRandomCarResId())
            puzzle.streetSidesView.addView(view)
            carsOnLanes.add(CarViewAndPosition(view, Random.nextFloat()))
        }
        while (carsOnLanes.size > laneCount) {
            val view = carsOnLanes.removeLast().view
            puzzle.streetSidesView.removeView(view)
        }
    }

    /** Position the cars as calculated */
    private fun renderCars() {
        val laneWidth = roadLaneWidth
        val w = roadWidth
        val h = roadHeight
        if (laneWidth == 0 || h == 0) return

        val trafficDirection = if (isForwardTraffic) 1f else -1f
        val rightLanesOffset =
            if (!isShowingLaneMarkings) 1f * laneWidth * carsOnLanesLeft.size
            else if (isShowingBothSides) w / 2f
            else 0f
        val leftLanesOffset =
            if (!isShowingLaneMarkings || !isShowingBothSides) 0f
            else 1f * laneWidth * max(0, carsOnLanesRight.size - carsOnLanesLeft.size)

        val carWidth = ((1f - 2f * LANE_PADDING) * laneWidth).toInt()

        carsOnLanesRight.asReversed().forEachIndexed { index, (view, position) ->
            if (view.width != 0 && view.height != 0) {
                val trackHeight = view.height + h
                view.layoutParams = RelativeLayout.LayoutParams(carWidth, carWidth * view.height / view.width)
                view.translationX = rightLanesOffset + laneWidth * (index + LANE_PADDING)
                view.translationY = trackHeight * (if (isForwardTraffic) 1 - position else position) - view.height
                view.scaleY = trafficDirection
            }
        }
        carsOnLanesLeft.forEachIndexed { index, (view, position) ->
            if (view.width != 0 && view.height != 0) {
                val trackHeight = view.height + h
                view.layoutParams = RelativeLayout.LayoutParams(carWidth, carWidth * view.height / view.width)
                view.translationX = leftLanesOffset + laneWidth * (index + LANE_PADDING)
                view.translationY = trackHeight * (if (isForwardTraffic) position else 1 - position) - view.height
                view.scaleY = -trafficDirection
            }
        }
    }

    /** Simulate the cars driving up the road */
    private fun moveCars(deltaTime: Long) {
        val w = roadWidth
        val h = roadHeight
        if (w == 0 || h == 0) return
        /* the CAR_SPEED is "lane graphic squares per second". If the lane graphic is not a square
           we need to go faster/slower */
        val ratio = 1f * w / h
        val totalLanes = max(2, carsOnLanesLeft.size + carsOnLanesRight.size)
        val delta = CAR_SPEED * ratio * deltaTime/1000f / totalLanes

        val randomPerLane = Random(1)
        val allCarsOnLanes = carsOnLanesLeft.asSequence() + carsOnLanesRight.asSequence()
        for(carViewAndPosition in allCarsOnLanes) {
            carViewAndPosition.position += delta * (1f + CAR_SPEED_VARIATION * (1 - 2 * randomPerLane.nextFloat()))
            if (carViewAndPosition.position > 1f) {
                carViewAndPosition.view.setImageResource(getRandomCarResId())
                carViewAndPosition.position = -Random.nextFloat() * CAR_SPARSITY
            }
        }
    }

    //endregion
}

private const val LANE_PADDING = 0.15f // as fraction of lane width
private const val CAR_SPEED = 5f // in "lane graphic squares per second"
private const val CAR_SPEED_VARIATION = 0.3f // as fraction: 1 = 100% variation
private const val CAR_SPARSITY = 1f

private fun getRandomCarResId(): Int = CAR_RES_IDS[Random.nextInt(CAR_RES_IDS.size)]

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

private data class CarViewAndPosition(val view: ImageView, var position: Float)
