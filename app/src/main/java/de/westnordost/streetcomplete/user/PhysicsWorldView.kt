package de.westnordost.streetcomplete.user

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import org.jbox2d.collision.AABB
import org.jbox2d.common.Transform
import org.jbox2d.dynamics.Body
import kotlin.collections.HashMap

/** Draws its contained views that are connected each with a physics body at the configured
 *  scale and location */
class PhysicsWorldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var offsetInMetersX = 0f
    var offsetInMetersY = 0f
    var pixelsPerMeter = 1f

    private val bodies: MutableMap<View, Body> = HashMap()

    // reused structs to avoid new object construction in loops
    private val identity = Transform()
    private val aabb = AABB()

    init {
        setWillNotDraw(false)
    }

    fun addView(view: View, body: Body) {
        bodies[view] = body
        addView(view, WRAP_CONTENT, WRAP_CONTENT)
    }

    override fun removeView(view: View) {
        bodies.remove(view)
        super.removeView(view)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((view, body) in bodies.entries) {
            val pixelWidth = view.width.toFloat()
            val pixelHeight = view.height.toFloat()
            if (pixelWidth == 0f || pixelHeight == 0f) continue

            val bbox = body.computeBoundingBox() ?: continue
            val widthInMeters = bbox.upperBound.x - bbox.lowerBound.x
            val heightInMeters = bbox.upperBound.y - bbox.lowerBound.y

            view.scaleX = widthInMeters * pixelsPerMeter / pixelWidth
            view.scaleY = heightInMeters * pixelsPerMeter / pixelHeight

            val centerInMeters = body.position
            view.x = +(centerInMeters.x - offsetInMetersX) * pixelsPerMeter - pixelWidth / 2f
            // ui coordinate system: +y = down, physics coordinate system: +y = up
            view.y = -(centerInMeters.y - offsetInMetersY) * pixelsPerMeter - pixelHeight / 2f + height

            view.rotation = -body.angle * 180f / Math.PI.toFloat()
        }
    }

    private fun Body.computeBoundingBox(): AABB? {
        // this function is less computational effort than it looks: No new objects are created
        // and for most bodies, it is just one fixture with one shape.
        var result: AABB? = null
        val identity = identity
        var fixture = fixtureList
        // fixtureList is an old-school C-like linked list, hence the odd iteration
        while (fixture != null) {
            val shape = fixture.shape
            for (i in 0 until shape.childCount) {
                val boundingBox = aabb
                shape.computeAABB(boundingBox, identity, i)
                if (result == null) result = boundingBox else result.combine(boundingBox)
            }
            fixture = fixture.next
        }
        return result
    }
}
