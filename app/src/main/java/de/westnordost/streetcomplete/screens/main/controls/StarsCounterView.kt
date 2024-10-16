package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.databinding.ViewAnswersCounterBinding

/** View that displays the user's quest answer counter */
class StarsCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAnswersCounterBinding.inflate(LayoutInflater.from(context), this)

    var starsCount: Int = 0
        set(value) {
            field = value
            binding.textView.text = value.toString()
        }

    var showProgress: Boolean = false
        set(value) {
            field = value
            binding.progressView.isInvisible = !value
        }

    var showLabel: Boolean
        set(value) { binding.labelView.isGone = !value }
        get() = binding.labelView.isGone

    fun setUploadedCount(uploadedCount: Int, animate: Boolean) {
        if (this.starsCount < uploadedCount && animate) {
            animateChange()
        }
        this.starsCount = uploadedCount
    }

    private fun animateChange() {
        binding.textView.animate()
            .scaleX(1.6f).scaleY(1.6f)
            .setInterpolator(DecelerateInterpolator(2f))
            .setDuration(100)
            .withEndAction {
                binding.textView.animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator()).duration = 100
            }
    }
}
