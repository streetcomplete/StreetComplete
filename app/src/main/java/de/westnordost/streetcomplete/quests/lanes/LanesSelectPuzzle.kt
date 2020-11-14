package de.westnordost.streetcomplete.quests.lanes

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import kotlinx.android.synthetic.main.lanes_select_puzzle.view.*
import kotlin.math.*
import kotlin.random.Random

class LanesSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), LifecycleObserver {

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
                isClickable = false
                leftSideClickArea.setOnClickListener { value.invoke(false) }
                rightSideClickArea.setOnClickListener { value.invoke(true) }
            }
        }

    var onClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                setOnClickListener(null)
                isClickable = false
            } else {
                leftSideClickArea.isClickable = false
                rightSideClickArea.isClickable = false
                setOnClickListener { value.invoke() }
            }
        }

    var isShowingLaneMarkings: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            invalidate()
        }
    }
    var laneCountLeft: Int = 0
    private set

    var laneCountRight: Int = 0
    private set

    var hasCenterLeftTurnLane: Boolean = false

    var isShowingBothSides: Boolean = true
    set(value) {
        if (field != value) {
            field = value
            updateLanes()
        }
    }

    var isForwardTraffic: Boolean = true

    private val roadPaint = Paint().also {
        it.color = Color.parseColor("#808080")
        it.style = Paint.Style.FILL
    }

    private val yellowLanePaint = Paint().also {
        it.color = Color.parseColor("#ffff00")
        it.style = Paint.Style.STROKE
        it.isAntiAlias = true
    }

    private val whiteLanePaint = Paint().also {
        it.color = Color.parseColor("#ffffff")
        it.style = Paint.Style.STROKE
        it.isAntiAlias = true
    }

    private val carsOnLanesLeft = mutableListOf<CarState>()
    private val carsOnLanesRight = mutableListOf<CarState>()

    private val carBitmaps: List<Bitmap>

    private val isShowingOnlyRightSide: Boolean get() = !isShowingBothSides || !isShowingLaneMarkings && laneCountLeft == 0

    private val noSidesAreDefined: Boolean get() = laneCountLeft == 0 && laneCountRight == 0
    private val bothSidesAreDefined: Boolean get() = laneCountLeft > 0 && laneCountRight > 0

    private val laneCountCenter: Int get() = if (hasCenterLeftTurnLane) 1 else 0

    private val leftLanesStart: Float get() = SHOULDER_WIDTH

    private val leftLanesEnd: Float get() = leftLanesStart + when {
        bothSidesAreDefined    -> laneCountLeft
        isShowingOnlyRightSide -> 0
        noSidesAreDefined      -> 1
        else                   -> max(laneCountLeft, laneCountRight)
    }

    private val lanesSpace: Int get() = laneCountCenter + when {
        bothSidesAreDefined    -> laneCountLeft + laneCountRight
        isShowingOnlyRightSide -> max(1, laneCountRight)
        noSidesAreDefined      -> 2
        else                   -> 2 * max(laneCountLeft, laneCountRight)
    }

    private val rightLanesStart: Float get() = leftLanesEnd + laneCountCenter

    private val laneWidth: Float get() = width / (lanesSpace + SHOULDER_WIDTH * 2)

    init {
        setWillNotDraw(false)

        carBitmaps = CAR_RES_IDS.map { resources.getBitmapDrawable(it).bitmap }

        LayoutInflater.from(context).inflate(R.layout.lanes_select_puzzle, this, true)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        leftSideTextView.setText(defaultTitle)
        rightSideTextView.setText(defaultTitle)

        animator.setTimeListener { _, _, deltaTime ->
            moveCars(deltaTime)
            invalidate()
        }

        addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                updateLanes()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) fun resume() {
        animator.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE) fun pause() {
        animator.end()
    }

    fun setLaneCounts(left: Int, right: Int, centerLeftTurn: Boolean) {
        laneCountLeft = left
        laneCountRight = right
        hasCenterLeftTurnLane = centerLeftTurn
        updateCarsOnLanes(left, carsOnLanesLeft)
        updateCarsOnLanes(right, carsOnLanesRight)

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        leftSideTextView.setText(if (laneCountLeft > 0) null else defaultTitle)
        rightSideTextView.setText(if (laneCountRight > 0) null else defaultTitle)

        updateLanes()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#33666666"))

        val laneWidth = laneWidth
        val lanesSpace = lanesSpace
        val leftLanesStart = leftLanesStart
        val leftLanesEnd = leftLanesEnd
        val rightLanesStart = rightLanesStart
        val shoulderWidth = SHOULDER_WIDTH * laneWidth

        // draw background
        val streetStartX = if (laneCountLeft > 0 || isShowingOnlyRightSide) 0f else laneWidth * leftLanesEnd
        val streetEndX = if (laneCountRight > 0) width.toFloat() else laneWidth * rightLanesStart
        canvas.drawRect(streetStartX, 0f, streetEndX, height.toFloat(), roadPaint)

        // draw markings:

        // 1. markings for the shoulders
        if (laneCountLeft > 0 || isShowingOnlyRightSide) {
            val leftShoulderX = leftLanesStart * laneWidth
            canvas.drawVerticalLine(leftShoulderX, whiteLanePaint)
        }
        if (laneCountRight > 0) {
            val rightShoulderX = shoulderWidth + lanesSpace * laneWidth
            canvas.drawVerticalLine(rightShoulderX, whiteLanePaint)
        }

        // 2. center line
        if (bothSidesAreDefined && !hasCenterLeftTurnLane && laneCountLeft + laneCountRight > 2 && isShowingLaneMarkings) {
            val offsetX = leftLanesEnd * laneWidth
            canvas.drawVerticalLine(offsetX, whiteLanePaint)
        }

        // 3. lane markings
        if (isShowingLaneMarkings) {
            for (x in 0 until laneCountLeft) {
                canvas.drawVerticalDashedLine(shoulderWidth + x * laneWidth, whiteLanePaint)
            }
            for (x in 0 until laneCountRight) {
                canvas.drawVerticalDashedLine((rightLanesStart + x) * laneWidth, whiteLanePaint)
            }
        }

        // 4. center turn lane markings
        if (hasCenterLeftTurnLane) {
            canvas.drawVerticalLine(leftLanesEnd * laneWidth, yellowLanePaint)
            canvas.drawVerticalLine(rightLanesStart * laneWidth, yellowLanePaint)
            canvas.drawVerticalDashedLine((leftLanesEnd + 0.125f) * laneWidth, yellowLanePaint)
            canvas.drawVerticalDashedLine((rightLanesStart - 0.125f) * laneWidth, yellowLanePaint)
        }

        // 5. draw cars
        carsOnLanesRight.asReversed().forEachIndexed { index, carState ->
            canvas.drawCar(carState, rightLanesStart + index, laneWidth, isForwardTraffic)
        }
        carsOnLanesLeft.forEachIndexed { index, carState ->
            canvas.drawCar(carState, leftLanesStart + index, laneWidth, !isForwardTraffic)
        }
    }

    /** update the lanes on the street */
    private fun updateLanes() {
        val w = width
        if (w == 0) return
        val laneWidth = laneWidth
        if (laneWidth == 0f) return

        whiteLanePaint.strokeWidth = LANE_MARKING_WIDTH * laneWidth
        yellowLanePaint.strokeWidth = LANE_MARKING_WIDTH * laneWidth

        leftSideClickArea.isGone = isShowingOnlyRightSide
        leftSideClickArea.updateLayoutParams {
            width = (leftLanesEnd * laneWidth).toInt()
        }
        rightSideClickArea.updateLayoutParams {
            width = w - (rightLanesStart * laneWidth).toInt()
        }

        invalidate()
    }

    /** initialize/delete car states given the new lane count */
    private fun updateCarsOnLanes(laneCount: Int, carsOnLanes: MutableList<CarState>) {
        while (carsOnLanes.size < laneCount) {
            carsOnLanes.add(CarState(carBitmaps[Random.nextInt(carBitmaps.size)], Random.nextFloat()))
        }
        while (carsOnLanes.size > laneCount) {
            carsOnLanes.removeLast()
        }
    }

    /** Simulate the cars driving up the road */
    private fun moveCars(deltaTime: Long) {
        val w = width
        val h = height
        if (w == 0 || h == 0) return
        /* the CAR_SPEED is "lane graphic squares per second". If the lane graphic is not a square
           we need to go faster/slower */
        val ratio = 1f * w / h
        val zoom = max(2, lanesSpace)
        val delta = CAR_SPEED * ratio * deltaTime/1000f / zoom

        val randomPerLane = Random(1)
        val allCarsOnLanes = carsOnLanesLeft.asSequence() + carsOnLanesRight.asSequence()
        for(carAndPosition in allCarsOnLanes) {
            carAndPosition.position += delta * (1f + CAR_SPEED_VARIATION * (1 - 2 * randomPerLane.nextFloat()))
            if (carAndPosition.position > 1f) {
                carAndPosition.bitmap = carBitmaps[Random.nextInt(carBitmaps.size)]
                carAndPosition.position = -Random.nextFloat() * CAR_SPARSITY
            }
        }
    }
}

private const val SHOULDER_WIDTH = 0.125f // as fraction of lane width
private const val LANE_MARKING_WIDTH = 0.0625f // as fraction of lane width
private const val CAR_LANE_PADDING = 0.10f // how much space there is between car and lane markings, as fraction of lane width
private const val CAR_SPEED = 5f // in "lane graphic squares per second"
private const val CAR_SPEED_VARIATION = 0.2f // as fraction: 1 = 100% variation
private const val CAR_SPARSITY = 1f

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

private data class CarState(var bitmap: Bitmap, var position: Float, val matrix: Matrix = Matrix())

private fun Canvas.drawVerticalDashedLine(x: Float, paint: Paint) {
    val w = paint.strokeWidth
    if (w == 0f) return
    val h = w * 6
    var y = 0f
    while (y < height) {
        drawLine(x, y, x, y + h, paint)
        y += 2 * h
    }
}

private fun Canvas.drawVerticalLine(x: Float, paint: Paint) {
    drawLine(x, 0f, x, height.toFloat(), paint)
}

private fun Canvas.drawCar(carState: CarState, laneIndex: Float, laneWidth: Float, isDirectionForward: Boolean) {
    val bitmap = carState.bitmap
    val w = bitmap.width
    val h = bitmap.height

    val carWidth = ((1f - 2f * CAR_LANE_PADDING) * laneWidth).toInt()
    val carHeight = carWidth * h / w

    val x = laneWidth * (laneIndex + CAR_LANE_PADDING)
    val y = (carHeight + height) * (if (!isDirectionForward) carState.position else 1 - carState.position) - carHeight

    carState.matrix.reset()
    if (!isDirectionForward) carState.matrix.postRotate(180f, w/2f, h/2f)
    carState.matrix.postScale(1f * carWidth / w, 1f * carHeight / h)
    carState.matrix.postTranslate(x, y)
    drawBitmap(bitmap, carState.matrix, null)
}
