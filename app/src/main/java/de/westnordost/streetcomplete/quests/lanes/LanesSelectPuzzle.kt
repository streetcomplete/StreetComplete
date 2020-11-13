package de.westnordost.streetcomplete.quests.lanes

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.StreetRotateable
import kotlinx.android.synthetic.main.lanes_select_puzzle.view.*
import kotlin.math.*
import kotlin.random.Random

class LanesSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), StreetRotateable, LifecycleObserver {

    private val animator = TimeAnimator()

    var onClickSideListener: ((isRight: Boolean) -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                leftSideClickArea.setOnClickListener(null)
                rightSideClickArea.setOnClickListener(null)
                leftSideClickArea.isClickable = false
                rightSideClickArea.isClickable = false
            } else {
                rotateContainer.isClickable = false
                leftSideClickArea.setOnClickListener { value.invoke(false) }
                rightSideClickArea.setOnClickListener { value.invoke(true) }
            }
        }

    var onClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                rotateContainer.setOnClickListener(null)
                rotateContainer.isClickable = false
            } else {
                leftSideClickArea.isClickable = false
                rightSideClickArea.isClickable = false
                rotateContainer.setOnClickListener { value.invoke() }
            }
        }

    var isShowingLaneMarkings: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            updateLanes()
        }
    }
    var laneCountLeft: Int = 0
    private set

    var laneCountRight: Int = 0
    private set

    var isShowingBothSides: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            updateLanes()
        }
    }

    var isForwardTraffic: Boolean = true

    private val carsOnLanesLeft = mutableListOf<CarViewAndPosition>()
    private val carsOnLanesRight = mutableListOf<CarViewAndPosition>()

    private val bothSidesAreDefined: Boolean get() = laneCountLeft > 0 && laneCountRight > 0
    private val isShowingOnlyRightSide: Boolean get() = !isShowingBothSides || !isShowingLaneMarkings && laneCountLeft == 0

    private val shoulderWidth = 0.125f

    private val laneWidth: Int get() {
        val w = rotateContainer.width
        val ratio = when {
            bothSidesAreDefined    -> laneCountLeft + laneCountRight
            isShowingOnlyRightSide -> max(laneCountRight, 1)
            else                   -> 2 * max(laneCountLeft, laneCountRight)
        } + shoulderWidth * 2
        return (w / ratio).toInt()
    }

    private val leftLanesOffset: Float get() = shoulderWidth

    private val rightLanesOffset: Float get() =
        shoulderWidth + when {
            bothSidesAreDefined    -> laneCountLeft
            isShowingOnlyRightSide -> 0
            else                   -> max(laneCountLeft, laneCountRight)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.lanes_select_puzzle, this, true)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        leftSideTextView.setText(defaultTitle)
        rightSideTextView.setText(defaultTitle)

        animator.setTimeListener { _, _, deltaTime ->
            moveCars(deltaTime)
            renderCars()
        }

        addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = min(bottom - top, right - left)
            val height = max(bottom - top, right - left)
            val params = rotateContainer.layoutParams
            if(width != params.width || height != params.height) {
                params.width = width
                params.height = height
                rotateContainer.layoutParams = params
                doOnNextLayout {
                    updateLanes()
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) fun resume() {
        animator.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE) fun pause() {
        animator.end()
    }

    override fun setStreetRotation(rotation: Float) {
        rotateContainer.rotation = rotation
        val scale = abs(cos(rotation * PI / 180)).toFloat()
        rotateContainer.scaleX = 1 + scale * 2 / 3f
        rotateContainer.scaleY = 1 + scale * 2 / 3f
    }

    fun setLaneCounts(left: Int, right: Int) {
        laneCountLeft = left
        laneCountRight = right
        updateCarViewsOnLanes(left, carsOnLanesLeft)
        updateCarViewsOnLanes(right, carsOnLanesRight)
        updateLanes()
    }

    //region lanes

    /** update the lanes on the street */
    private fun updateLanes() {
        if (rotateContainer.width == 0) return
        val bitmap = createLanesBitmap(rotateContainer.width, laneCountLeft, laneCountRight, isShowingOnlyRightSide, isShowingLaneMarkings)
        val drawable = bitmap?.let { BitmapDrawable(resources, it) }
        drawable?.tileModeY = Shader.TileMode.REPEAT
        streetView.setImageDrawable(drawable)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        leftSideTextView.setText(if (laneCountLeft > 0) null else defaultTitle)
        rightSideTextView.setText(if (laneCountRight > 0) null else defaultTitle)

        val centerX = (rightLanesOffset * laneWidth).toInt()
        leftSideClickArea.isGone = isShowingOnlyRightSide
        leftSideClickArea.updateLayoutParams { width = centerX }
        rightSideClickArea.updateLayoutParams { width = rotateContainer.width - centerX }

        renderCars()
    }

    // endregion

    //region cars

    /** initialize/delete car views given the new lane count */
    private fun updateCarViewsOnLanes(laneCount: Int, carsOnLanes: MutableList<CarViewAndPosition>) {
        while (carsOnLanes.size < laneCount) {
            val view = ImageView(context)
            view.scaleType = ImageView.ScaleType.CENTER_INSIDE
            view.setImageResource(getRandomCarResId())
            rotateContainer.addView(view)
            carsOnLanes.add(CarViewAndPosition(view, Random.nextFloat()))
        }
        while (carsOnLanes.size > laneCount) {
            val view = carsOnLanes.removeLast().view
            rotateContainer.removeView(view)
        }
    }

    /** Position the cars as calculated */
    private fun renderCars() {
        val w = rotateContainer.width
        val h = rotateContainer.height
        if (w == 0 || h == 0) return
        val l = laneCountLeft
        val r = laneCountRight
        if (l == 0 && r == 0) return

        val laneWidth = laneWidth
        val rightLanesOffset = rightLanesOffset
        val leftLanesOffset = leftLanesOffset

        val carWidth = ((1f - 2f * LANE_PADDING) * laneWidth).toInt()
        val trafficDirection = if (isForwardTraffic) 1f else -1f

        carsOnLanesRight.asReversed().forEachIndexed { index, (view, position) ->
            val dw = view.drawable.intrinsicWidth
            val dh = view.drawable.intrinsicHeight
            if (dw != 0 && dh != 0) {
                val carHeight = carWidth * dh / dw
                if (carWidth != view.width || carHeight != view.height)
                    view.layoutParams = RelativeLayout.LayoutParams(carWidth, carHeight)
                view.translationX = laneWidth * (rightLanesOffset + index + LANE_PADDING)
                view.translationY = (carHeight + h) * (if (isForwardTraffic) 1 - position else position) - carHeight
                view.scaleY = trafficDirection
            }
        }

        carsOnLanesLeft.forEachIndexed { index, (view, position) ->
            val dw = view.drawable.intrinsicWidth
            val dh = view.drawable.intrinsicHeight
            if (dw != 0 && dh != 0) {
                val carHeight = carWidth * dh / dw
                if (carWidth != view.width || carHeight != view.height)
                    view.layoutParams = RelativeLayout.LayoutParams(carWidth, carHeight)
                view.translationX = laneWidth * (leftLanesOffset + index + LANE_PADDING)
                view.translationY = (carHeight + h) * (if (isForwardTraffic) position else 1 - position) - carHeight
                view.scaleY = -trafficDirection
            }
        }
    }

    /** Simulate the cars driving up the road */
    private fun moveCars(deltaTime: Long) {
        val w = rotateContainer.width
        val h = rotateContainer.height
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

private const val LANE_PADDING = 0.10f // as fraction of lane width
private const val CAR_SPEED = 5f // in "lane graphic squares per second"
private const val CAR_SPEED_VARIATION = 0.2f // as fraction: 1 = 100% variation
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
