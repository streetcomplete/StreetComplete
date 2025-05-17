package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.spToPx

/** A dialog in which you can select one value from a range of values. If a custom layout is supplied,
 *  it must have a NumberPicker with the id "numberPicker". */
class ValuePickerDialog<T>(
    context: Context,
    private val values: List<T>,
    selectedValue: T? = null,
    title: CharSequence? = null,
    @LayoutRes layoutResId: Int = R.layout.dialog_number_picker,
    private val callback: (value: T) -> Unit
) : AlertDialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(layoutResId, null)
        setView(view)
        setTitle(title)

        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            callback(values[numberPicker.value])
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
            cancel()
        }
        numberPicker.wrapSelectorWheel = false
        numberPicker.displayedValues = values.map { it.toString() }.toTypedArray()
        numberPicker.minValue = 0
        numberPicker.maxValue = values.size - 1
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            numberPicker.textSize = context.resources.spToPx(32)
        }
        selectedValue?.let { numberPicker.value = values.indexOf(it) }
        // do not allow keyboard input
        numberPicker.disableEditTextsFocus()
    }

    private fun ViewGroup.disableEditTextsFocus() {
        for (child in children) {
            if (child is ViewGroup) {
                child.disableEditTextsFocus()
            } else if (child is EditText) {
                child.isFocusable = false
            }
        }
    }
}
