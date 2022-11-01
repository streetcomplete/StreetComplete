package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.databinding.ViewUploadButtonBinding

/** A view that shows an upload-icon, with a counter at the top right and an (upload) progress view
 *  */
class UploadButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding = ViewUploadButtonBinding.inflate(LayoutInflater.from(context), this)

    var uploadableCount: Int = 0
        set(value) {
            field = value
            binding.textView.text = value.toString()
            binding.textView.isInvisible = value == 0
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.iconView.alpha = if (enabled) 1f else 0.5f
    }

    init {
        clipToPadding = false
    }
}
