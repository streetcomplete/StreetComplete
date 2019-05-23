package de.westnordost.streetcomplete.settings

import androidx.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.NumberPicker

import de.westnordost.streetcomplete.R

class NumberPickerPreferenceDialog : PreferenceDialogFragmentCompat() {
    private lateinit var picker: NumberPicker
    private lateinit var values: Array<String>

    private val pref: NumberPickerPreference
        get() = preference as NumberPickerPreference


    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker = view.findViewById(R.id.numberPicker)
        values = (pref.maxValue..pref.minValue step pref.step).map { "$it" }.toTypedArray()
        var currentValue = values.indexOf(pref.value.toString())
        if(currentValue == -1) currentValue = 0
	    picker.apply {
            displayedValues = values
		    minValue = 0
		    maxValue = values.size - 1
		    value = currentValue
		    wrapSelectorWheel = false
	    }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // hackfix: The Android number picker accepts input via soft keyboard (which makes sense
        // from a UX viewpoint) but is not designed for that. By default, it does not apply the
        // input there. See http://stackoverflow.com/questions/18944997/numberpicker-doesnt-work-with-keyboard
        // A workaround is to clear the focus before saving.
        picker.clearFocus()

        if (positiveResult) {
            pref.value = values.indexOf(picker.value.toString())
        }
    }
}
