package de.westnordost.streetcomplete.controls

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.view_upload_button.view.*

class UploadButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr)  {

    var uploadableCount: Int = 0
    set(value) {
        field = value
        textView.text = value.toString()
        textView.visibility = if (value == 0) View.INVISIBLE else View.VISIBLE
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        iconView.alpha = if (enabled) 1f else 0.5f
    }

    var showProgress: Boolean = false
    set(value) {
        field = value
        progressView.visibility = if(value) View.VISIBLE else View.INVISIBLE
    }

    init {
        inflate(context, R.layout.view_upload_button, this)
        clipToPadding = false
    }
}