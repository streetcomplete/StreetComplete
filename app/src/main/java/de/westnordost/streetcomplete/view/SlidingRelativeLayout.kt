package de.westnordost.streetcomplete.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/** A relative layout that can be animated via an ObjectAnimator on the yFraction and xFraction
 * properties. I.e., it can be animated to slide up and down, left and right */
class SlidingRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    var yFraction: Float = 0f
        set(fraction) {
            field = fraction
            if (height != 0) translationY = height * yFraction
        }
    var xFraction: Float = 0f
        set(fraction) {
            field = fraction
            if (width != 0) translationX = width * xFraction
        }
}
