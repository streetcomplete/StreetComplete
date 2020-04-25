package de.westnordost.streetcomplete.controls

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.view_icon_progress.view.*

/** Shows an icon, surrounded by a circular progress bar and a finished-checkmark */
class IconProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr)  {

    private var wobbleAnimator: Animator? = null

    var icon: Drawable?
        set(value) { iconView.setImageDrawable(value) }
        get() = iconView.drawable


    fun showProgressAnimation() {
        wobbleAnimator = AnimatorInflater.loadAnimator(context, R.animator.progress_wobble).apply {
            setTarget(iconView)
            start()
        }
        progressView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    fun showFinishedAnimation() {
        wobbleAnimator?.cancel()
        progressView.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
        checkmarkView.animate()
            .setDuration(300)
            .alpha(1f)
            .start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (checkmarkView.drawable as? AnimatedVectorDrawable)?.start()
        }
    }

    init {
        inflate(context, R.layout.view_icon_progress, this)
    }
}