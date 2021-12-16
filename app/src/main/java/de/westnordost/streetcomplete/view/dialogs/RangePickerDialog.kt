package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogRangePickerBinding

typealias RangePickedCallback = (startIndex: Int, endIndex: Int) -> Unit

/** A dialog in which you can select a range of values  */
class RangePickerDialog(
    context: Context,
    values: Array<String>,
    startIndex: Int?,
    endIndex: Int?,
    title: CharSequence,
    private val callback: RangePickedCallback
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    init {
        val binding = DialogRangePickerBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)
        setTitle(title)

        val startPicker = binding.startPicker
        val endPicker = binding.endPicker

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
        for (child in children) {
            if (child is ViewGroup) {
                child.disableEditTextsFocus()
            } else if (child is EditText) {
                child.isFocusable = false
            }
        }
    }
}
