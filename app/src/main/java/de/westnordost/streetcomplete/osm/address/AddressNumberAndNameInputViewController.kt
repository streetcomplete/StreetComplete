package de.westnordost.streetcomplete.osm.address

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

/** Manages the UI for inputting the address number (usually housenumber, see [AddressStreetNameInputViewController])
 *  and/or a house name.
 *
 *  Both inputs can be retracted and expanded with tapping on [toggleHouseNameButton] or
 *  [toggleAddressNumberButton], respectively. By default, the [houseNameInput] is retracted, i.e.
 *  not visible while [addressNumberContainer] is visible. Each input is expanded automatically if
 *  there is any text inside and cannot be retracted as long as there is any text inside.
 *  */
class AddressNumberAndNameInputViewController(
    private val toggleHouseNameButton: TextView?,
    private val houseNameInput: EditText,
    private val toggleAddressNumberButton: TextView?,
    private val addressNumberContainer: View,
    activity: Activity,
    houseNumberInput: EditText?,
    blockNumberInput: EditText?,
    conscriptionNumberInput: EditText?,
    streetNumberInput: EditText?,
    toggleKeyboardButton: Button,
    addButton: View?,
    subtractButton: View?
) {
    private val addressNumberCtrl = AddressNumberInputViewController(
        activity, houseNumberInput, blockNumberInput, conscriptionNumberInput, streetNumberInput, toggleKeyboardButton, addButton, subtractButton
    )

    var onInputChanged: (() -> Unit)? = null

    var houseName: String?
        get() = houseNameInput.nonBlankTextOrNull
        set(value) { houseNameInput.setText(value) }

    var addressNumber: AddressNumber?
        get() = addressNumberCtrl.addressNumber
        set(value) { addressNumberCtrl.addressNumber = value }

    val isEmpty: Boolean get() =
        addressNumberCtrl.isEmpty && houseName == null

    /** Whether the input is complete. It is never complete if the address is only partially filled */
    val isComplete: Boolean get() =
        addressNumber != null || addressNumberCtrl.isEmpty && houseName != null

    init {
        setHouseNameViewExpanded(false)
        setAddressNumberViewExpanded(true)

        toggleHouseNameButton?.setOnClickListener {
            setHouseNameViewExpanded(houseNameInput.isGone)
        }

        toggleAddressNumberButton?.setOnClickListener {
            setAddressNumberViewExpanded(addressNumberContainer.isGone)
        }

        addressNumberCtrl.onInputChanged = {
            val number = addressNumber
            toggleAddressNumberButton?.isEnabled = number == null
            if (number != null) setAddressNumberViewExpanded(true)
            onInputChanged?.invoke()
        }
        houseNameInput.doAfterTextChanged {
            val name = houseName
            toggleHouseNameButton?.isEnabled = name == null
            if (name != null) setHouseNameViewExpanded(true)
            onInputChanged?.invoke()
        }
    }

    fun setHouseNameViewExpanded(expand: Boolean) {
        houseNameInput.isGone = !expand
        val arrow = getArrowDrawableResId(expand)
        toggleHouseNameButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(arrow, 0, 0, 0)
    }

    fun setAddressNumberViewExpanded(expand: Boolean) {
        addressNumberContainer.isGone = !expand
        val arrow = getArrowDrawableResId(expand)
        toggleAddressNumberButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(arrow, 0, 0, 0)
    }

    private fun getArrowDrawableResId(expanded: Boolean) =
        if (expanded) R.drawable.ic_arrow_expand_down_24dp else R.drawable.ic_arrow_expand_right_24dp
}
