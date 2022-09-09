package de.westnordost.streetcomplete.osm.address

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddressNumberOrNameInputViewController(
    private val toggleHouseNameButton: TextView?,
    private val houseNameInput: EditText,
    private val toggleAddressNumberButton: TextView?,
    private val addressNumberContainer: View,
    private val addressNumberCtrl: AddressNumberInputViewController
) {
    var onInputChanged: (() -> Unit)? = null

    var houseName: String?
        get() = houseNameInput.nonBlankTextOrNull
        set(value) { houseNameInput.setText(value) }

    var addressNumber: AddressNumber?
        get() = addressNumberCtrl.addressNumber
        set(value) { addressNumberCtrl.addressNumber = value }

    val isEmpty: Boolean get() =
        addressNumberCtrl.isEmpty && houseName == null

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
            toggleAddressNumberButton?.isEnabled = addressNumber == null
            onInputChanged?.invoke()
        }
        houseNameInput.doAfterTextChanged {
            toggleHouseNameButton?.isEnabled = houseName == null
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
