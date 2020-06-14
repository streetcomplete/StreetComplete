package de.westnordost.streetcomplete.controls

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.view_answers_counter.view.*

class AnswersCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr)  {

    var uploadedCount: Int = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            textView.text = " $value "
        }

    var showProgress: Boolean = false
        set(value) {
            field = value
            progressView.visibility = if(value) View.VISIBLE else View.INVISIBLE
        }

    init {
        inflate(context, R.layout.view_answers_counter, this)
    }

    fun setUploadedCount(uploadedCount: Int, animate: Boolean) {
        if (this.uploadedCount < uploadedCount && animate) {
            animateChange()
        }
        this.uploadedCount = uploadedCount

    }

    private fun animateChange() {
        textView.animate()
            .scaleX(1.6f).scaleY(1.6f)
            .setInterpolator(DecelerateInterpolator(2f))
            .setDuration(100)
            .withEndAction {
                textView.animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator()).duration = 100
            }
    }
}