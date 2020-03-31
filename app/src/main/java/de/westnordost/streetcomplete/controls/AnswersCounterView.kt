package de.westnordost.streetcomplete.controls

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.view_answers_counter.view.*

class AnswersCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr)  {

    var uploadedCount: Int = 0
        set(value) {
            field = value
            textView.text = value.toString()
        }

    var showProgress: Boolean = false
        set(value) {
            field = value
            progressView.visibility = if(value) View.VISIBLE else View.INVISIBLE
        }

    init {
        inflate(context, R.layout.view_answers_counter, this)
    }
}