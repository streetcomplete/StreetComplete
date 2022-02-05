package de.westnordost.streetcomplete.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewLengthInputBinding
import de.westnordost.streetcomplete.ktx.intOrNull
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.osm.LengthUnit

/** Allows to input a length in any of the units specified in [selectableUnits] */
class LengthInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLengthInputBinding.inflate(LayoutInflater.from(context), this)

    var onInputChanged: (() -> Unit)? = null

    var feet: Int?
        get() = binding.feetInput.intOrNull
        set(value) { binding.feetInput.setText(value?.toString()) }

    var inches: Int?
        get() = binding.inchesInput.intOrNull
        set(value) { binding.inchesInput.setText(value?.toString()) }

    var meters: Double?
        get() = binding.metersInput.numberOrNull
        set(value) { binding.metersInput.setText(value?.toString()) }

    init {
        binding.unitSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateInputFieldsVisibility()
                onInputChanged?.invoke()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.inchesInput.filters = arrayOf(IntRangeInputValidator(0 until 12))

        binding.metersInput.addTextChangedListener { onInputChanged?.invoke() }
        binding.feetInput.addTextChangedListener { onInputChanged?.invoke() }
        binding.inchesInput.addTextChangedListener { onInputChanged?.invoke() }

        updateInputFieldsVisibility()
    }

    /** set/get which units can be selected from the dropdown */
    var selectableUnits: List<LengthUnit> = emptyList()
        set(value) {
            field = value
            binding.unitSelect.isEnabled = value.size > 1
            if (value.isEmpty()) {
                binding.unitSelect.adapter = null
            } else {
                binding.unitSelect.adapter = ArrayAdapter(context, R.layout.spinner_item_centered, value)
                binding.unitSelect.setSelection(0)
            }
            updateInputFieldsVisibility()
        }

    /** set/get which unit to use. Must be one of the units in the [selectableUnits] list */
    var unit: LengthUnit?
        get() = binding.unitSelect.selectedItem as LengthUnit?
        set(value) {
            val index = selectableUnits.indexOf(value)
            if (index != -1) {
                binding.unitSelect.setSelection(index)
            }
        }

    /** return the input length of the selected unit or null if input is not complete or invalid */
    val length: Length? get() = when (unit) {
        LengthUnit.METER -> {
            meters?.let { LengthInMeters(it) }
        }
        LengthUnit.FOOT_AND_INCH -> {
            val feet = feet
            val inches = inches
            if (feet != null && inches != null) LengthInFeetAndInches(feet, inches) else null
        }
        null -> null
    }

    private fun updateInputFieldsVisibility() {
        binding.feetInchesContainer.isInvisible = unit != LengthUnit.FOOT_AND_INCH
        binding.metersContainer.isInvisible = unit !=  LengthUnit.METER
    }
}
