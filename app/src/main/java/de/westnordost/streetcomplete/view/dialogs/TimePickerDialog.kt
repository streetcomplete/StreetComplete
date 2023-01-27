package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog

/** A dialog in which you can select a time */
class TimePickerDialog(
    context: Context,
    initialHourOfDay: Int,
    initialMinute: Int,
    is24HourView: Boolean,
    private val callback: (hourOfDay: Int, minute: Int) -> Unit
) : AlertDialog(context) {

    private val timePicker: TimePicker = TimePicker(context)

    init {
        timePicker.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        setView(timePicker)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || timePicker.validateInput()) {
                callback(timePicker.currentHour, timePicker.currentMinute)
                // Clearing focus forces the dialog to commit any pending
                // changes, e.g. typed text in a NumberPicker.
                timePicker.clearFocus()
                dismiss()
            }
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
            cancel()
        }
        timePicker.setIs24HourView(is24HourView)
        timePicker.currentHour = initialHourOfDay
        timePicker.currentMinute = initialMinute
    }

    fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
        timePicker.currentHour = hourOfDay
        timePicker.currentMinute = minuteOfHour
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(HOUR, timePicker.currentHour)
        state.putInt(MINUTE, timePicker.currentMinute)
        state.putBoolean(IS_24_HOUR, timePicker.is24HourView)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(HOUR)
        val minute = savedInstanceState.getInt(MINUTE)
        timePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR))
        timePicker.currentHour = hour
        timePicker.currentMinute = minute
    }

    companion object {
        private const val HOUR = "hour"
        private const val MINUTE = "minute"
        private const val IS_24_HOUR = "is24hour"
    }
}
