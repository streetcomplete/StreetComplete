package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.databinding.ViewMessagesButtonBinding

/** View that shows a messages-button with a little counter at the top right */
class MessagesButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMessagesButtonBinding.inflate(LayoutInflater.from(context), this)

    var messagesCount: Int = 0
        set(value) {
            field = value
            binding.textView.text = value.toString()
            binding.textView.isInvisible = value == 0
        }

    init {
        clipToPadding = false
    }
}
