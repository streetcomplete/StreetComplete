package de.westnordost.streetcomplete.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes

import de.westnordost.streetcomplete.R

/**
 * Preference that shows a simple number picker
 */
class NumberPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dialogPreferenceStyle,
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
