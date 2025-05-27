package de.westnordost.streetcomplete.view

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.RelativeLayout

class RelativeLayoutFix @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {

    // always dispatch window insets to children, see #6030
    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
            return super.dispatchApplyWindowInsets(insets)
        } else {
            for (i in 0 until childCount) {
                getChildAt(i).dispatchApplyWindowInsets(insets)
            }
            return insets
        }
    }
}
