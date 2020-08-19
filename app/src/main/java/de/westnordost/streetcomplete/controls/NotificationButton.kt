package de.westnordost.streetcomplete.controls

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.view_notification_button.view.*

class NotificationButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr)  {

    var notificationsCount: Int = 0
    set(value) {
        field = value
        textView.text = value.toString()
        textView.isInvisible = value == 0
    }

    init {
        inflate(context, R.layout.view_notification_button, this)
        clipToPadding = false
    }
}
