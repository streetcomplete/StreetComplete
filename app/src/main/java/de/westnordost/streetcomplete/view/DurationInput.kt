package de.westnordost.streetcomplete.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewDurationBinding
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.view.inputfilter.InputValidator
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits
import de.westnordost.streetcomplete.view.inputfilter.acceptIntDigits

/** Allows to input a duration, in days, hours or minutes */
class DurationInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDurationBinding.inflate(LayoutInflater.from(context), this)

    var onInputChanged: (() -> Unit)? = null

    var durationUnit: DurationUnit
        set(value) { binding.unitSelect.setSelection(value.ordinal) }
        get() = DurationUnit.values()[binding.unitSelect.selectedItemPosition]

    var durationValue: Double
        set(value) { binding.input.setText(value.toString()) }
        get() = binding.input.numberOrNull ?: 0.0

    init {
        binding.unitSelect.adapter = ArrayAdapter(
            context,
            R.layout.spinner_item_centered,
            DurationUnit.values().map { it.toLocalizedString(context.resources) }
        )
        if (binding.unitSelect.selectedItemPosition < 0) binding.unitSelect.setSelection(1)
        binding.unitSelect.onItemSelectedListener = OnAdapterItemSelectedListener {

            onInputChanged?.invoke()
        }
        binding.input.filters = arrayOf(acceptDecimalDigits(3, 1))
        binding.input.addTextChangedListener { onInputChanged?.invoke() }
    }

}

enum class DurationUnit { MINUTES, HOURS, DAYS }

private fun DurationUnit.toLocalizedString(resources: Resources) = when (this) {
    DurationUnit.MINUTES -> resources.getString(R.string.unit_minutes)
    DurationUnit.HOURS -> resources.getString(R.string.unit_hours)
    DurationUnit.DAYS -> resources.getString(R.string.unit_days)
}
