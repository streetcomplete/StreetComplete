package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker

import de.westnordost.streetcomplete.R

/** A dialog in which you can select one value from a range of values  */
class ValuePickerDialog(
    context: Context,
    values: Array<String>,
    selectedIndex: Int,
    title: CharSequence,
    private val callback: (value: Int) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
        setView(view)
        setTitle(title)

        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            callback(numberPicker.value)
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
            cancel()
        }
        numberPicker.wrapSelectorWheel = false
        numberPicker.displayedValues = values
        numberPicker.minValue = 0
        numberPicker.maxValue = values.size - 1
        numberPicker.value = selectedIndex
        // do not allow keyboard input
        numberPicker.disableEditTextsFocus()
    }

    private fun ViewGroup.disableEditTextsFocus() {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
                child.disableEditTextsFocus()
            } else if (child is EditText) {
                child.isFocusable = false
            }
        }
    }
}
