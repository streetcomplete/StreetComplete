package de.westnordost.streetcomplete.view.controller

import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.util.ktx.intOrNull
import de.westnordost.streetcomplete.util.ktx.numberOrNull
import de.westnordost.streetcomplete.view.OnAdapterItemSelectedListener
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits
import de.westnordost.streetcomplete.view.inputfilter.acceptIntDigits
import de.westnordost.streetcomplete.view.inputfilter.acceptIntRange

/** Manages inputting a length in any of the units specified in meters or feet+inch. */
class LengthInputViewController(
    private val unitSelect: Spinner,
    private val metersContainer: ViewGroup,
    private val metersInput: EditText,
    private val feetInchesContainer: ViewGroup,
    private val feetInput: EditText,
    private val inchesInput: EditText
) {

    var onInputChanged: (() -> Unit)? = null

    @LayoutRes var unitSelectItemResId: Int = R.layout.spinner_item_centered

    /** if true, only hides the unit select spinner if the only selectable unit is foot+inch.
     *  Otherwise, the unit select spinner is hidden if there is only one selectable unit. */
    var isCompactMode: Boolean = false
        set(value) {
            field = value
            updateUnitSelectVisibility()
        }

    /** set/get how many digits the feet input may have */
    var maxFeetDigits: Int = 4
        set(value) {
            field = value
            feetInput.filters = arrayOf(acceptIntDigits(value))
        }

    /** set/get how many digits the meter input may have (before and after the decimal separator) */
    var maxMeterDigits: Pair<Int, Int> = Pair(3, 2)
        set(value) {
            field = value
            metersInput.filters = arrayOf(acceptDecimalDigits(value.first, value.second))
        }

    /** set/get which units can be selected from the dropdown */
    var selectableUnits: List<LengthUnit> = LengthUnit.values().toList()
        set(value) {
            field = value
            unitSelect.isEnabled = value.size > 1
            updateUnitSelectVisibility()
            if (value.isEmpty()) {
                unitSelect.adapter = null
            } else {
                unitSelect.adapter = ArrayAdapter(unitSelect.context, unitSelectItemResId, value)
                unitSelect.setSelection(0)
            }
            updateInputFieldsVisibility()
        }

    /** set/get which unit to use. Must be one of the units in the [selectableUnits] list */
    var unit: LengthUnit?
        get() = unitSelect.selectedItem as LengthUnit?
        set(value) {
            val index = selectableUnits.indexOf(value)
            if (index != -1) {
                unitSelect.setSelection(index)
            }
        }

    /** get/set the input length of the selected unit or null if input is not complete or invalid */
    var length: Length?
        get() = when (unit) {
            LengthUnit.METER -> {
                metersInput.numberOrNull?.let { LengthInMeters(it) }
            }
            LengthUnit.FOOT_AND_INCH -> {
                val feet = feetInput.intOrNull
                val inches = inchesInput.intOrNull
                if (feet != null && inches != null) LengthInFeetAndInches(feet, inches) else null
            }
            null -> null
        }
        set(value) {
            when (value) {
                is LengthInFeetAndInches -> {
                    feetInput.setText(value.feet.toString())
                    inchesInput.setText(value.inches.toString())
                }
                is LengthInMeters -> {
                    metersInput.setText(value.meters.toString())
                }
                null -> {}
            }
        }

    init {
        unitSelect.onItemSelectedListener = OnAdapterItemSelectedListener {
            updateInputFieldsVisibility()
            onInputChanged?.invoke()
        }

        feetInput.filters = arrayOf(acceptIntDigits(4))
        inchesInput.filters = arrayOf(acceptIntRange(0 until 12))
        metersInput.filters = arrayOf(acceptDecimalDigits(3, 2))

        metersInput.doAfterTextChanged { onInputChanged?.invoke() }
        feetInput.doAfterTextChanged { onInputChanged?.invoke() }
        inchesInput.doAfterTextChanged { onInputChanged?.invoke() }

        updateInputFieldsVisibility()
    }

    private fun updateUnitSelectVisibility() {
        unitSelect.isGone = selectableUnits.size == 1 && (!isCompactMode || selectableUnits.singleOrNull() == LengthUnit.FOOT_AND_INCH)
    }

    private fun updateInputFieldsVisibility() {
        feetInchesContainer.isInvisible = unit != LengthUnit.FOOT_AND_INCH
        metersContainer.isInvisible = unit != LengthUnit.METER
        when (unit) {
            LengthUnit.METER -> metersInput
            LengthUnit.FOOT_AND_INCH -> feetInput
            null -> null
        }?.requestFocus()
    }
}
