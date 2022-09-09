package de.westnordost.streetcomplete.osm.address

import android.content.res.Resources
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController.StreetOrPlace.*
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.OnAdapterItemSelectedListener

//TODO click on map magic / road name suggestions...

/** Manages the input of a street name or alternatively (if not available) a place name. (Only one
 *  of the two can be set at a time) */
class StreetOrPlaceNameViewController(
    private val select: Spinner,
    private val streetNameInput: EditText,
    private val placeNameInput: EditText
) {
    var onInputChanged: (() -> Unit)? = null

    var streetOrPlaceName: StreetOrPlaceName?
        set(value) {
            when (value) {
                is PlaceName -> {
                    spinnerSelection = PLACE
                    placeNameInput.setText(value.name)
                }
                is StreetName -> {
                    spinnerSelection = STREET
                    streetNameInput.setText(value.name)
                }
                null -> {
                    streetNameInput.text = null
                    placeNameInput.text = null
                }
            }
        }
        get() = when (spinnerSelection) {
            STREET -> streetNameInput.nonBlankTextOrNull?.let { StreetName(it) }
            PLACE ->  placeNameInput.nonBlankTextOrNull?.let { PlaceName(it) }
        }

    private var spinnerSelection: StreetOrPlace
        set(value) { select.setSelection(StreetOrPlace.values().indexOf(value)) }
        get() = StreetOrPlace.values()[select.selectedItemPosition]

    init {
        select.adapter = ArrayAdapter(
            select.context,
            R.layout.spinner_item_centered,
            StreetOrPlace.values().map { it.toLocalizedString(select.context.resources) }
        )

        select.onItemSelectedListener = OnAdapterItemSelectedListener {
            updateInputs()
            onInputChanged?.invoke()
        }

        streetNameInput.doAfterTextChanged { onInputChanged?.invoke() }
        placeNameInput.doAfterTextChanged { onInputChanged?.invoke() }

        updateInputs()
    }

    private fun updateInputs() {
        val selection = spinnerSelection
        when (selection) {
            STREET -> placeNameInput.text = null
            PLACE ->  streetNameInput.text = null
        }
        streetNameInput.isGone = selection != STREET
        placeNameInput.isGone = selection != PLACE
    }

    private enum class StreetOrPlace { STREET, PLACE }

    private fun StreetOrPlace.toLocalizedString(resources: Resources) = when (this) {
        STREET -> resources.getString(R.string.quest_address_street_street_name_label)
        PLACE -> resources.getString(R.string.quest_address_street_place_name_label)
    }
}
