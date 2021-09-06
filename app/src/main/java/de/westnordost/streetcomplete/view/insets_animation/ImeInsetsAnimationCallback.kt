package de.westnordost.streetcomplete.view.insets_animation

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import de.westnordost.streetcomplete.ktx.setPadding

/** Make the keyboard appear and disappear smoothly. Must be set on both
 *  setOnApplyWindowInsetsListener and setWindowInsetsAnimationCallback */
class ImeInsetsAnimationCallback(
    private val view: View,
    private val onNewInsets: View.(insets: Insets) -> Unit
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE), OnApplyWindowInsetsListener {
    private var isAnimating = false
    private var prevInsets: WindowInsetsCompat? = null

    override fun onApplyWindowInsets(v: View, windowInsets: WindowInsetsCompat): WindowInsetsCompat {
        prevInsets = windowInsets
        if (!isAnimating) applyNewInsets(windowInsets)
        return windowInsets
    }

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            isAnimating = true
        }
    }

    override fun onProgress(insets: WindowInsetsCompat, runningAnims: List<WindowInsetsAnimationCompat>): WindowInsetsCompat {
        applyNewInsets(insets)
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (isAnimating && (animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
            isAnimating = false
            prevInsets?.let { view.dispatchApplyWindowInsets(it.toWindowInsets()) }
        }
    }

    private fun applyNewInsets(insets: WindowInsetsCompat) {
        val typeInsets = insets.getInsets(WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars())
        view.onNewInsets(typeInsets)
    }
}

fun View.respectSystemInsets(onNewInsets: View.(insets: Insets) -> Unit = View::setPadding) {
    val imeAnimationCallback = ImeInsetsAnimationCallback(this, onNewInsets)
    ViewCompat.setOnApplyWindowInsetsListener(this, imeAnimationCallback)
    ViewCompat.setWindowInsetsAnimationCallback(this, imeAnimationCallback)
}
