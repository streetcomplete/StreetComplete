package de.westnordost.streetcomplete.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.RelativeLayout

class SlidingRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    var yFraction: Float = 0f
        set(fraction) {
            field = fraction
            postAfterViewMeasured { translationY = height * yFraction }
        }
    var xFraction: Float = 0f
        set(fraction) {
            field = fraction
            postAfterViewMeasured { translationX = width * xFraction }
        }

    private fun postAfterViewMeasured(callback: () -> Unit) {
        if (width != 0 || height != 0) {
            callback()
        } else {
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        callback()
                        if (viewTreeObserver.isAlive) {
                            viewTreeObserver.removeOnPreDrawListener(this)
                        }
                        return true
                    }
                })
            }
        }
    }
}
