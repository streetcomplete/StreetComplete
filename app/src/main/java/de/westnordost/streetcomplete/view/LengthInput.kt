package de.westnordost.streetcomplete.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.databinding.ViewLengthInputBinding
import de.westnordost.streetcomplete.ktx.intOrNull
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits
import de.westnordost.streetcomplete.view.inputfilter.acceptIntDigits
import de.westnordost.streetcomplete.view.inputfilter.acceptIntRange

/** Allows to input a length in any of the units specified in [selectableUnits] */
class LengthInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLengthInputBinding.inflate(LayoutInflater.from(context), this)

    var onInputChanged: (() -> Unit)? = null

    init {
        binding.unitSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateInputFieldsVisibility()
                onInputChanged?.invoke()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.feetInput.filters = arrayOf(acceptIntDigits(4))
        binding.inchesInput.filters = arrayOf(acceptIntRange(0 until 12))
        binding.metersInput.filters = arrayOf(acceptDecimalDigits(3, 2))

        binding.metersInput.addTextChangedListener { onInputChanged?.invoke() }
        binding.feetInput.addTextChangedListener { onInputChanged?.invoke() }
        binding.inchesInput.addTextChangedListener { onInputChanged?.invoke() }

        updateInputFieldsVisibility()
    }

    /** set/get how many digits the feet input may have*/
    var maxFeetDigits: Int = 4
        set(value) {
            field = value
            binding.feetInput.filters = arrayOf(acceptIntDigits(value))
        }

    /** set/get how many digits the meter input may have*/
    var maxMeterDigitsBeforeDecimalPoint: Int = 3
        set(value) {
            field = value
            binding.metersInput.filters = arrayOf(acceptDecimalDigits(value, 2))
        }

    /** set/get which units can be selected from the dropdown */
    var selectableUnits: List<LengthUnit> = emptyList()
        set(value) {
            field = value
            binding.unitSelect.isEnabled = value.size > 1
            binding.unitSelect.isGone = value.singleOrNull() == LengthUnit.FOOT_AND_INCH
            if (value.isEmpty()) {
                binding.unitSelect.adapter = null
            } else {
                binding.unitSelect.adapter = ArrayAdapter(context, R.layout.spinner_item_centered_large, value)
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

    /** get/set the input length of the selected unit or null if input is not complete or invalid */
    var length: Length?
        get() = when (unit) {
            LengthUnit.METER -> {
                binding.metersInput.numberOrNull?.let { LengthInMeters(it) }
            }
            LengthUnit.FOOT_AND_INCH -> {
                val feet = binding.feetInput.intOrNull
                val inches = binding.inchesInput.intOrNull
                if (feet != null && inches != null) LengthInFeetAndInches(feet, inches) else null
            }
            null -> null
        }
        set(value) {
            when (value) {
                is LengthInFeetAndInches -> {
                    binding.feetInput.setText(value.feet.toString())
                    binding.inchesInput.setText(value.inches.toString())
                }
                is LengthInMeters -> {
                    binding.metersInput.setText(value.meters.toString())
                }
                null -> {}
            }
        }

    private fun updateInputFieldsVisibility() {
        binding.feetInchesContainer.isInvisible = unit != LengthUnit.FOOT_AND_INCH
        binding.metersContainer.isInvisible = unit != LengthUnit.METER
        when (unit) {
            LengthUnit.METER -> binding.metersInput
            LengthUnit.FOOT_AND_INCH -> binding.feetInput
            null -> null
        }?.requestFocus()
    }
}
