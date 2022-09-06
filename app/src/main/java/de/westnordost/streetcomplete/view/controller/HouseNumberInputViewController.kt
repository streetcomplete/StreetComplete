package de.westnordost.streetcomplete.view.controller

import android.app.Activity
import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.osm.housenumber.AddressNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseNumber
import de.westnordost.streetcomplete.osm.housenumber.addToHouseNumber
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.showKeyboard

/** Manages inputting a house number with optional block number (Japan).
 *  The [lastHouseNumber] is shown as a hint and the user can use the [addButton] or
 *  [subtractButton] to count up or down from the previous value without having to open the
 *  keyboard for [houseNumberInput].
 *  The [lastBlockNumber] is used to prefill the [blockNumberInput]. */
class HouseNumberInputViewController(
    activity: Activity,
    private val houseNumberInput: EditText,
    private val blockNumberInput: EditText?,
    toggleKeyboardButton: Button,
    addButton: View?,
    subtractButton: View?,
    private val lastBlockNumber: String?,
    private val lastHouseNumber: String?
) {

    private val toggleKeyboardButtonViewController: SwitchKeyboardButtonViewController

    private var houseNumberInputTextColors: ColorStateList? = null
    // because the hint is implemented as a hack: it is actually the text proper but colored in light-gray
    private val houseNumberInputText: String? get() =
        houseNumberInput.nonBlankTextOrNull?.takeIf { houseNumberInputTextColors == null }

    var onInputChanged: (() -> Unit)? = null

    val isEmpty: Boolean get() =
        houseNumberInputText == null
        && blockNumberInput?.nonBlankTextOrNull == null

    val addressNumber: AddressNumber? get() {
        val houseNumber = houseNumberInputText ?: return null
        val blockNumber = blockNumberInput?.nonBlankTextOrNull
        return when {
            blockNumber != null -> HouseAndBlockNumber(houseNumber, blockNumber)
            else ->                HouseNumber(houseNumber)
        }
    }

    init {
        addButton?.setOnClickListener { addToHouseNumberInput(+1) }
        subtractButton?.setOnClickListener { addToHouseNumberInput(-1) }

        // must be called before registering the text changed watchers because it changes the text
        prefillBlockNumber()

        houseNumberInput.doAfterTextChanged { onInputChanged?.invoke() }
        blockNumberInput?.doAfterTextChanged { onInputChanged?.invoke() }

        toggleKeyboardButtonViewController = SwitchKeyboardButtonViewController(activity, toggleKeyboardButton, setOfNotNull(houseNumberInput, blockNumberInput))

        // must be after initKeyboardButton+textchanged watchers because it re-sets the onFocusListener
        showHouseNumberHint()
    }

    private fun prefillBlockNumber() {
        /* the block number likely does not change from one input to the other, so let's prefill it
           with the last selected value */
        lastBlockNumber?.let { blockNumberInput?.setText(it) }
    }

    private fun showHouseNumberHint() {
        val prev = lastHouseNumber ?: return

        /* The Auto fit layout does not work with hints, so we workaround this by setting the "real"
        *  text instead and make it look like it is a hint. This little hack is much less effort
        *  than to fork and fix the external dependency. We need to revert back the color both on
        *  focus and on text changed (tapping on +/- button) */
        houseNumberInputTextColors = houseNumberInput.textColors
        houseNumberInput.setTextColor(houseNumberInput.hintTextColors)
        houseNumberInput.setText(prev)
        houseNumberInput.doAfterTextChanged {
            val colors = houseNumberInputTextColors
            if (colors != null) houseNumberInput.setTextColor(colors)
            houseNumberInputTextColors = null
        }
        houseNumberInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            toggleKeyboardButtonViewController.updateVisibility()
            if (hasFocus) houseNumberInput.showKeyboard()
            val colors = houseNumberInputTextColors
            if (hasFocus && colors != null) {
                houseNumberInput.text = null
                houseNumberInput.setTextColor(colors)
                houseNumberInputTextColors = null
            }
        }
    }

    private fun addToHouseNumberInput(add: Int) {
        val input = houseNumberInput
        val prev = input.text.toString().ifBlank { lastHouseNumber } ?: return
        val newHouseNumber = addToHouseNumber(prev, add) ?: return
        input.setText(newHouseNumber)
        input.setSelection(newHouseNumber.length)
    }
}
