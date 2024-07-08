package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker
import androidx.core.content.withStyledAttributes
import androidx.preference.PreferenceDialogFragmentCompat
import de.westnordost.streetcomplete.R

/**
 * Preference that shows a simple number picker
 */
class NumberPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0
) : DialogPreferenceCompat(context, attrs, defStyleAttr, defStyleRes) {

    private var _value: Int = 0
    var value: Int
        get() = _value
        set(v) {
            _value = v
            persistInt(v)
            notifyChanged()
        }

    var minValue: Int = DEFAULT_MIN_VALUE
        private set
    var maxValue: Int = DEFAULT_MAX_VALUE
        private set
    var step: Int = STEP
        private set

    init {
        dialogLayoutResource = R.layout.dialog_number_picker_preference

        context.withStyledAttributes(attrs, R.styleable.NumberPickerPreference) {
            minValue = getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE)
            maxValue = getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE)
            step = getInt(R.styleable.NumberPickerPreference_step, STEP)
        }
    }

    override fun createDialog() = NumberPickerPreferenceDialog()

    @Deprecated("Deprecated in Java")
    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        val defaultInt = defaultValue as? Int ?: DEFAULT_VALUE
        _value = if (restorePersistedValue) getPersistedInt(defaultInt) else defaultInt
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getInteger(index, DEFAULT_VALUE)

    override fun getSummary() = String.format(super.getSummary().toString(), value)

    companion object {
        private const val DEFAULT_MIN_VALUE = 1
        private const val DEFAULT_MAX_VALUE = 100
        private const val STEP = 1

        private const val DEFAULT_VALUE = 1
    }
}

class NumberPickerPreferenceDialog : PreferenceDialogFragmentCompat() {
    private lateinit var picker: NumberPicker
    private lateinit var values: Array<String>

    private val pref: NumberPickerPreference
        get() = preference as NumberPickerPreference

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker = view.findViewById(R.id.numberPicker)
        val intValues = (pref.minValue..pref.maxValue step pref.step).toList()
        values = intValues.map { "$it" }.toTypedArray()
        var index = values.indexOf(pref.value.toString())
        if (index == -1) {
            do ++index while (index < intValues.lastIndex && intValues[index] < pref.value)
        }
        picker.apply {
            displayedValues = values
            minValue = 0
            maxValue = values.size - 1
            value = index
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
            pref.value = values[picker.value].toInt()
        }
    }
}
