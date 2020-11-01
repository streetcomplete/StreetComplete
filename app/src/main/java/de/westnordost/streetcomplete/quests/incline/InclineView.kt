package de.westnordost.streetcomplete.quests.incline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.fromDegreesToPercentage
import kotlin.math.abs
import kotlin.math.roundToInt


class InclineView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onLockChanged(locked: Boolean)
    }

    private var listeners: MutableList<Listener> = mutableListOf()

    var locked: Boolean = false
    var inclineInDegrees: Double = 0.0
    private var lastRenderedInclineInDegrees: Double = 0.0

    private val backgroundPaint = Paint()
    private val foregroundPaint = Paint()
    private val textPaint = Paint()

    @ColorInt private var lockedColorInt = ContextCompat.getColor(context, R.color.accent)
    @ColorInt private var unlockedColorInt = ContextCompat.getColor(context, R.color.primary)

    init {
        backgroundPaint.color = ContextCompat.getColor(context, R.color.inverted_background)
        foregroundPaint.color = unlockedColorInt

        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 32f * resources.displayMetrics.scaledDensity

        val clickAnimation = AlphaAnimation(1f, 0.1f).apply {
            duration = 150
            interpolator = LinearInterpolator()
        }

        setOnClickListener {
            changeLock(!locked)
            startAnimation(clickAnimation)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        this.lastRenderedInclineInDegrees = this.inclineInDegrees
        drawVisualRepresentationOfDegrees(canvas)
        drawInclineInPercentageLabel(canvas)
        drawLockIcon(canvas)
    }

    private fun drawVisualRepresentationOfDegrees(canvas: Canvas) {
        canvas.drawRect(
            0f, 0f,
            width.toFloat(), height.toFloat(),
            backgroundPaint)
        canvas.save()

        canvas.rotate((-inclineInDegrees).toFloat(), width.toFloat() / 2f, height.toFloat() / 2f)

        canvas.drawRect(
            -width.toFloat(), height.toFloat() / 2f,
            width.toFloat() * 4f, height.toFloat() * 4f,
            foregroundPaint)
        canvas.restore()
    }

    private fun drawInclineInPercentageLabel(canvas: Canvas) {
        val label = when {
                inclineInDegrees > 89.0 -> "∞ %"
                inclineInDegrees < -89.0 -> "-∞ %"
                else -> "%.0f".format(inclineInDegrees.fromDegreesToPercentage()) + " %"
            }

        canvas.drawText(
            label,
            width / 2f,
            ((height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)),
            textPaint)
    }

    private fun drawLockIcon(canvas: Canvas) {
        val lockIconResource = if (locked) R.drawable.ic_lock else R.drawable.ic_lock_open_24
        val lockIconDrawable: Drawable? = ContextCompat.getDrawable(context, lockIconResource)
        val iconSizeDp = (24 * resources.displayMetrics.scaledDensity).roundToInt()
        val offsetDp = (8 * resources.displayMetrics.scaledDensity).roundToInt()
        lockIconDrawable?.setBounds(
            width - (iconSizeDp + offsetDp),
            offsetDp,
            width - offsetDp,
            iconSizeDp + offsetDp)
        lockIconDrawable?.draw(canvas)
    }

    fun changeIncline(inclineInDegrees: Double) {
        val shouldInvalidate = abs(this.lastRenderedInclineInDegrees - inclineInDegrees) >= 0.5
        this.inclineInDegrees = inclineInDegrees

        if (shouldInvalidate) {
            invalidate()
        }
    }

    fun changeLock(locked: Boolean) {
        this.locked = locked
        foregroundPaint.color = if (locked) lockedColorInt else unlockedColorInt
        invalidate()

        listeners.forEach {
            it.onLockChanged(locked)
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}
