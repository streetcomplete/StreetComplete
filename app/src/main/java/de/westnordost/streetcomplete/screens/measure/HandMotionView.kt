package de.westnordost.streetcomplete.screens.measure

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import de.westnordost.streetcomplete.databinding.ViewHandMotionBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** View that shows a hand with smartphone doing a slow circling motion in the same way as it is
 *  shown when using sceneform-ux but with a different graphic (and less bloated code, ahem) */
class HandMotionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val handView: ImageView

    init {
        val binding = ViewHandMotionBinding.inflate(LayoutInflater.from(context), this)
        handView = binding.handMotionView

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
                animate(interpolatedTime)
            }
        }
        animation.startOffset = 1000
        animation.duration = 2500
        animation.repeatCount = Animation.INFINITE
        handView.startAnimation(animation)
    }

    fun animate(time: Float) {
        val startAngle = PI / 2
        val progressAngle = PI * 2 * time
        val currentAngle = startAngle + progressAngle
        val radius = handView.resources.displayMetrics.density * 25
        val centerX = this.width / 2 - handView.width / 2
        val centerY = this.height / 2 - handView.height / 2

        handView.x = (radius * 2 * cos(currentAngle) + centerX).toFloat()
        handView.y = (radius * sin(currentAngle) + centerY).toFloat()
        handView.invalidate()
    }
}
