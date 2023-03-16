package de.westnordost.streetcomplete.osm.address

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.nonBlankHintOrNull
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.controller.SwitchKeyboardButtonViewController

/** Manages the UI for inputting EITHER:
 *  - a house number,
 *  - a house number with block number (as used in Japan) or a
 *  - conscription number with optional street (=orientation) number (as used in Czechia, Slovakia)
 *
 *  Which "mode" is being used depends on which inputs are not null.
 *  */
class AddressNumberInputViewController(
    activity: Activity,
    private val houseNumberInput: EditText?,
    private val blockNumberInput: EditText?,
    private val blockInput: EditText?,
    private val conscriptionNumberInput: EditText?,
    private val streetNumberInput: EditText?,
    toggleKeyboardButton: Button,
    addButton: View?,
    subtractButton: View?
) {
    private val toggleKeyboardButtonViewController: SwitchKeyboardButtonViewController

    var onInputChanged: (() -> Unit)? = null

    val isEmpty: Boolean get() =
        houseNumberInput?.nonBlankTextOrNull == null
        && blockNumberInput?.nonBlankTextOrNull == null
        && blockInput?.nonBlankTextOrNull == null
        && conscriptionNumberInput?.nonBlankTextOrNull == null
        && streetNumberInput?.nonBlankTextOrNull == null

    var addressNumber: AddressNumber?
        get() {
            val conscriptionNumber = conscriptionNumberInput?.nonBlankTextOrNull
            if (conscriptionNumber != null) {
                val streetNumber = streetNumberInput?.nonBlankTextOrNull
                return ConscriptionNumber(conscriptionNumber, streetNumber)
            }

            val houseNumber = houseNumberInput?.nonBlankTextOrNull ?: return null
            return when {
                blockNumberInput != null -> blockNumberInput.nonBlankTextOrNull?.let { HouseAndBlockNumber(houseNumber, it) }
                blockInput != null -> blockInput.nonBlankTextOrNull?.let { HouseNumberAndBlock(houseNumber, it) }
                else -> HouseNumber(houseNumber)
            }
        }
        set(value) {
            when (value) {
                is HouseAndBlockNumber -> {
                    houseNumberInput?.setText(value.houseNumber)
                    blockNumberInput?.setText(value.blockNumber)
                }
                is HouseNumberAndBlock -> {
                    houseNumberInput?.setText(value.houseNumber)
                    blockInput?.setText(value.block)
                }
                is HouseNumber -> {
                    houseNumberInput?.setText(value.houseNumber)
                }
                is ConscriptionNumber -> {
                    conscriptionNumberInput?.setText(value.conscriptionNumber)
                    streetNumberInput?.setText(value.streetNumber)
                }
                null -> {
                    houseNumberInput?.text = null
                    blockNumberInput?.text = null
                    blockInput?.text = null
                    conscriptionNumberInput?.text = null
                    streetNumberInput?.text = null
                }
            }
        }

    init {
        addButton?.setOnClickListener { addToHouseNumberInput(+1) }
        subtractButton?.setOnClickListener { addToHouseNumberInput(-1) }
        houseNumberInput?.doAfterTextChanged {
            // changing the house number is interpreted as acknowledging the block number hint
            if (blockNumberInput != null) {
                if (blockNumberInput.nonBlankTextOrNull == null) {
                    if (blockNumberInput.nonBlankHintOrNull != null) {
                        blockNumberInput.setText(blockNumberInput.hint)
                    }
                }
            }
            // same for block
            if (blockInput != null) {
                if (blockInput.nonBlankTextOrNull == null) {
                    if (blockInput.nonBlankHintOrNull != null) {
                        blockInput.setText(blockInput.hint)
                    }
                }
            }
            onInputChanged?.invoke()
        }
        blockNumberInput?.doAfterTextChanged { onInputChanged?.invoke() }
        blockInput?.doAfterTextChanged { onInputChanged?.invoke() }
        conscriptionNumberInput?.doAfterTextChanged { onInputChanged?.invoke() }
        streetNumberInput?.doAfterTextChanged { onInputChanged?.invoke() }

        toggleKeyboardButtonViewController = SwitchKeyboardButtonViewController(
            activity, toggleKeyboardButton, setOfNotNull(houseNumberInput, blockNumberInput, streetNumberInput)
        ) // blockInput is missing because it should always show the full keyboard

        blockInput?.let { editText ->
            val ctx = editText.context
            val formWidth = ctx.resources.getDimension(R.dimen.quest_form_width).toInt().takeIf { it > 0 }
                ?: ctx.resources.displayMetrics.widthPixels
            // form width minus 250dp, but max. 150dp
            editText.maxWidth = (formWidth - ctx.dpToPx(250).toInt())
                .coerceAtMost(ctx.dpToPx(150).toInt())
                .coerceAtLeast(ctx.dpToPx(56).toInt())
        }
    }

    private fun addToHouseNumberInput(add: Int) {
        val input = houseNumberInput ?: return
        val prev = input.nonBlankTextOrNull ?: input.nonBlankHintOrNull ?: return
        val newHouseNumber = addToHouseNumber(prev, add) ?: return
        input.setText(newHouseNumber)
        input.setSelection(newHouseNumber.length)
    }
}
