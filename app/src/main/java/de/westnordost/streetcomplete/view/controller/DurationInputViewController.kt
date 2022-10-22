package de.westnordost.streetcomplete.view.controller

import android.content.res.Resources
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.numberOrNull
import de.westnordost.streetcomplete.view.OnAdapterItemSelectedListener
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits

/** Manages inputting a duration - in days, hours or minutes */
class DurationInputViewController(
    private val unitSelect: Spinner,
    private val input: EditText
) {
    var onInputChanged: (() -> Unit)? = null

    var durationUnit: DurationUnit
        set(value) { unitSelect.setSelection(value.ordinal) }
        get() = DurationUnit.values()[unitSelect.selectedItemPosition]

    var durationValue: Double
        set(value) { input.setText(value.toString()) }
        get() = input.numberOrNull ?: 0.0

    init {
        unitSelect.adapter = ArrayAdapter(
            unitSelect.context,
            R.layout.spinner_item_centered,
            DurationUnit.values().map { it.toLocalizedString(unitSelect.context.resources) }
        )
        if (unitSelect.selectedItemPosition < 0) unitSelect.setSelection(1)
        unitSelect.onItemSelectedListener = OnAdapterItemSelectedListener {
            onInputChanged?.invoke()
        }
        input.filters = arrayOf(acceptDecimalDigits(3, 1))
        input.doAfterTextChanged { onInputChanged?.invoke() }
    }
}

enum class DurationUnit { MINUTES, HOURS, DAYS }

private fun DurationUnit.toLocalizedString(resources: Resources) = when (this) {
    DurationUnit.MINUTES -> resources.getString(R.string.unit_minutes)
    DurationUnit.HOURS -> resources.getString(R.string.unit_hours)
    DurationUnit.DAYS -> resources.getString(R.string.unit_days)
}
