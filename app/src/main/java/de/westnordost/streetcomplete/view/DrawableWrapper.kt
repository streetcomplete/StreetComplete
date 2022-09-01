package de.westnordost.streetcomplete.view

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.Drawable

/** Base class for any wrapper drawable. I.e. a drawable that wraps another drawable to modify its
 *  behavior. */
open class DrawableWrapper(val drawable: Drawable) : Drawable(), Drawable.Callback {
    override fun draw(canvas: Canvas) { drawable.draw(canvas) }
    override fun onBoundsChange(bounds: Rect) { drawable.bounds = bounds }
    override fun setChangingConfigurations(configs: Int) { drawable.changingConfigurations = configs }
    override fun getChangingConfigurations(): Int = drawable.changingConfigurations
    @Deprecated("Deprecated in Java")
    override fun setDither(dither: Boolean) { drawable.setDither(dither) }
    override fun setFilterBitmap(filter: Boolean) { drawable.isFilterBitmap = filter }
    override fun setAlpha(alpha: Int) { drawable.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { drawable.colorFilter = cf }
    override fun isStateful(): Boolean = drawable.isStateful
    override fun setState(stateSet: IntArray): Boolean = drawable.setState(stateSet)
    override fun getState(): IntArray = drawable.state
    override fun jumpToCurrentState() { drawable.jumpToCurrentState() }
    override fun getCurrent(): Drawable = drawable.current
    override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        super.setVisible(visible, restart) || drawable.setVisible(visible, restart)
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = drawable.getOpacity()
    override fun getTransparentRegion(): Region? = drawable.transparentRegion
    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth
    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight
    override fun getMinimumWidth(): Int = drawable.minimumWidth
    override fun getMinimumHeight(): Int = drawable.minimumHeight
    override fun getPadding(padding: Rect): Boolean = drawable.getPadding(padding)
    override fun invalidateDrawable(who: Drawable) { invalidateSelf() }
    override fun scheduleDrawable(who: Drawable, what: Runnable, at: Long) { scheduleSelf(what, at) }
    override fun unscheduleDrawable(who: Drawable, what: Runnable) { unscheduleSelf(what) }
    override fun onLevelChange(level: Int): Boolean = drawable.setLevel(level)
    override fun setAutoMirrored(mirrored: Boolean) { drawable.isAutoMirrored = mirrored }
    override fun isAutoMirrored(): Boolean = drawable.isAutoMirrored
    override fun setTint(tint: Int) { drawable.setTint(tint) }
    override fun setTintList(tint: ColorStateList?) { drawable.setTintList(tint) }
    override fun setTintMode(tintMode: PorterDuff.Mode?) { drawable.setTintMode(tintMode) }
    override fun setHotspot(x: Float, y: Float) { drawable.setHotspot(x, y) }
    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        drawable.setHotspotBounds(left, top, right, bottom)
    }
}
