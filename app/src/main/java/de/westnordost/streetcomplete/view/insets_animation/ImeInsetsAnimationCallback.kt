package de.westnordost.streetcomplete.view.insets_animation

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.annotation.RequiresApi

/** Make the keyboard appear and disappear smoothly. Must be set on both
 *  setOnApplyWindowInsetsListener and setWindowInsetsAnimationCallback */
@RequiresApi(Build.VERSION_CODES.R)
class ImeInsetsAnimationCallback(
    private val view: View,
    private val onNewInsets: View.(left: Int, top: Int, right: Int, bottom: Int) -> Unit
) : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE), View.OnApplyWindowInsetsListener {

    private var isAnimating = false
    private var prevInsets: WindowInsets? = null

    override fun onApplyWindowInsets(v: View, windowInsets: WindowInsets): WindowInsets {
        prevInsets = windowInsets
        if (!isAnimating) applyNewInsets(windowInsets)
        return windowInsets
    }

    override fun onPrepare(animation: WindowInsetsAnimation) {
        if (animation.typeMask and WindowInsets.Type.ime() != 0) {
            isAnimating = true
        }
    }

    override fun onProgress(insets: WindowInsets, runningAnims: List<WindowInsetsAnimation>): WindowInsets {
        applyNewInsets(insets)
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimation) {
        if (isAnimating && (animation.typeMask and WindowInsets.Type.ime()) != 0) {
            isAnimating = false
            prevInsets?.let { view.dispatchApplyWindowInsets(it) }
        }
    }

    private fun applyNewInsets(insets: WindowInsets) {
        val typeInsets = insets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemBars())
        onNewInsets(view, typeInsets.left, typeInsets.top, typeInsets.right, typeInsets.bottom)
    }
}

fun View.respectSystemInsets(onNewInsets: View.(left: Int, top: Int, right: Int, bottom: Int) -> Unit = View::setPadding) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val imeAnimationCallback = ImeInsetsAnimationCallback(this, onNewInsets)
        setOnApplyWindowInsetsListener(imeAnimationCallback)
        setWindowInsetsAnimationCallback(imeAnimationCallback)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        setOnApplyWindowInsetsListener { v, windowInsets ->
            onNewInsets(v,
                windowInsets.systemWindowInsetLeft,
                windowInsets.systemWindowInsetTop,
                windowInsets.systemWindowInsetRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets
        }
    }
}
