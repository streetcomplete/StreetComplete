package de.westnordost.streetcomplete.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.doOnPreDraw

/** A relative layout that can be animated via an ObjectAnimator on the yFraction and xFraction
 * properties */
class SlidingRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    var yFraction: Float = 0f
        set(fraction) {
            field = fraction
            doOnPreDraw { translationY = height * yFraction }
        }
    var xFraction: Float = 0f
        set(fraction) {
            field = fraction
            doOnPreDraw { translationX = width * xFraction }
        }
}
