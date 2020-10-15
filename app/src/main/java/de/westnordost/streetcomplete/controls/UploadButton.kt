package de.westnordost.streetcomplete.controls

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
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
        textView.isInvisible = value == 0
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        iconView.alpha = if (enabled) 1f else 0.5f
    }

    var showProgress: Boolean = false
    set(value) {
        field = value
        progressView.isInvisible = !value
    }

    init {
        inflate(context, R.layout.view_upload_button, this)
        clipToPadding = false
    }
}
