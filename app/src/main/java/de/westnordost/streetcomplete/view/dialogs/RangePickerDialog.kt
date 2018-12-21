package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText

import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.dialog_range_picker.*

typealias RangePickedCallback = (startIndex: Int, endIndex: Int) -> Unit

class RangePickerDialog(
    context: Context,
    values: Array<String>,
    startIndex: Int?,
    endIndex: Int?,
    title: CharSequence,
    private val callback: RangePickedCallback
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_range_picker, null)
        setView(view)
        setTitle(title)

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
	        callback(startPicker.value, endPicker.value)
	        dismiss()
        }
	    setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
		    cancel()
	    }

        startPicker.wrapSelectorWheel = false
        startPicker.displayedValues = values
        startPicker.minValue = 0
        startPicker.maxValue = values.size - 1
        startPicker.value = startIndex ?: 0

        endPicker.wrapSelectorWheel = false
        endPicker.displayedValues = values
        endPicker.minValue = 0
        endPicker.maxValue = values.size - 1
        endPicker.value = endIndex ?: values.size - 1

        // do not allow keyboard input
	    startPicker.disableEditTextsFocus()
	    endPicker.disableEditTextsFocus()
    }

    private fun ViewGroup.disableEditTextsFocus() {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
	            child.disableEditTextsFocus()
            } else if (child is EditText) {
                child.setFocusable(false)
            }
        }
    }
}
