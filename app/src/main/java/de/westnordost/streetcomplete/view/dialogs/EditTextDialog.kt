package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

/** A dialog in which you input a text */
class EditTextDialog(
    context: Context,
    title: CharSequence? = null,
    text: String? = null,
    @LayoutRes layoutResId: Int = R.layout.dialog_edit_text,
    private val callback: (value: String) -> Unit
) : AlertDialog(context) {

    val editText: EditText

    init {
        val view = LayoutInflater.from(context).inflate(layoutResId, null)
        setView(view)
        setTitle(title)

        editText = view.findViewById(R.id.editText)
        editText.setText(text)
        editText.doAfterTextChanged {
            getButton(BUTTON_POSITIVE)?.isEnabled = editText.nonBlankTextOrNull != null
        }

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            callback(editText.nonBlankTextOrNull!!)
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
            cancel()
        }
    }
}
