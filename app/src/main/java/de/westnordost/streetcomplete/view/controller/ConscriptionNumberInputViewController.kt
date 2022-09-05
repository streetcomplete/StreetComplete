package de.westnordost.streetcomplete.view.controller

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.osm.housenumber.ConscriptionNumber
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

/** Manages inputting a conscription number + optional street number */
class ConscriptionNumberInputViewController(
    activity: Activity,
    private val conscriptionNumberInput: EditText,
    private val streetNumberInput: EditText,
    toggleKeyboardButton: Button,
) {
    private val toggleKeyboardButtonViewController: SwitchKeyboardButtonViewController

    var onInputChanged: (() -> Unit)? = null

    val isEmpty: Boolean get() =
        conscriptionNumberInput.nonBlankTextOrNull == null
        && streetNumberInput.nonBlankTextOrNull == null

    val conscriptionNumber: ConscriptionNumber? get() {
        val conscriptionNumber = conscriptionNumberInput.nonBlankTextOrNull ?: return null
        val streetNumber = streetNumberInput.nonBlankTextOrNull
        return ConscriptionNumber(conscriptionNumber, streetNumber)
    }

    init {
        conscriptionNumberInput.doAfterTextChanged { onInputChanged?.invoke() }
        streetNumberInput.doAfterTextChanged { onInputChanged?.invoke() }
        toggleKeyboardButtonViewController = SwitchKeyboardButtonViewController(activity, toggleKeyboardButton, setOf(streetNumberInput))
    }
}
