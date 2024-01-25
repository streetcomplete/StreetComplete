package de.westnordost.streetcomplete.osm.address

import android.content.res.Resources
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController.StreetOrPlace.*
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.OnAdapterItemSelectedListener
import java.util.Locale

/** Manages the input of a street name or alternatively (if not available) a place name.
 *
 *  Only one of the two can be set at a time. While there is text in either input field, the user
 *  cannot switch to the other input field. */
class StreetOrPlaceNameViewController(
    private val select: Spinner,
    private val placeNameInputContainer: View,
    private val placeNameInput: EditText,
    private val streetNameInputContainer: View,
    private val streetNameInput: EditText,
    roadNameSuggestionsSource: RoadNameSuggestionsSource,
    abbreviationsByLocale: AbbreviationsByLocale,
    countryLocale: Locale,
    startWithPlace: Boolean,
) {
    private val streetNameInputCtrl = AddressStreetNameInputViewController(
        streetNameInput, roadNameSuggestionsSource, abbreviationsByLocale, countryLocale
    )

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
                    streetNameInputCtrl.streetName = value.name
                }
                null -> {
                    spinnerSelection = STREET
                    streetNameInputCtrl.streetName = null
                    placeNameInput.text = null
                }
            }
        }
        get() = when (spinnerSelection) {
            STREET -> streetNameInputCtrl.streetName?.let { StreetName(it) }
            PLACE ->  placeNameInput.nonBlankTextOrNull?.let { PlaceName(it) }
        }

    private var spinnerSelection: StreetOrPlace
        set(value) { select.setSelection(entries.indexOf(value)) }
        get() = StreetOrPlace.entries[select.selectedItemPosition]

    init {
        select.adapter = ArrayAdapter(
            select.context,
            R.layout.spinner_item,
            StreetOrPlace.entries.map { it.toLocalizedString(select.context.resources) }
        )
        spinnerSelection = if (startWithPlace) PLACE else STREET

        select.onItemSelectedListener = OnAdapterItemSelectedListener {
            updateInputVisibilities()
            updateSpinnerEnablement()
            onInputChanged?.invoke()
        }

        placeNameInput.doAfterTextChanged {
            updateSpinnerEnablement()
            onInputChanged?.invoke()
        }
        streetNameInputCtrl.onInputChanged = {
            updateSpinnerEnablement()
            onInputChanged?.invoke()
        }

        updateInputVisibilities()
        updateSpinnerEnablement()
    }

    fun selectStreetAt(position: LatLon, radiusInMeters: Double): Boolean {
        if (spinnerSelection != STREET) return false

        streetNameInputCtrl.selectStreetAt(position, radiusInMeters)
        return true
    }

    fun selectPlaceName() {
        spinnerSelection = PLACE
        placeNameInput.requestFocus()
    }

    fun applyHint() {
        when (spinnerSelection) {
            STREET -> {
                if (streetNameInput.hint != null && streetNameInput.nonBlankTextOrNull == null) {
                    streetNameInput.setText(streetNameInput.hint)
                }
            }
            PLACE -> {
                if (placeNameInput.hint != null && placeNameInput.nonBlankTextOrNull == null) {
                    placeNameInput.setText(placeNameInput.hint)
                }
            }
        }
    }

    private fun updateSpinnerEnablement() {
        select.isEnabled = when (spinnerSelection) {
            STREET -> streetNameInputCtrl.streetName == null
            PLACE -> placeNameInput.nonBlankTextOrNull == null
        }
    }

    private fun updateInputVisibilities() {
        val selection = spinnerSelection
        streetNameInputContainer.isGone = selection != STREET
        placeNameInputContainer.isGone = selection != PLACE
    }

    private enum class StreetOrPlace { STREET, PLACE }

    private fun StreetOrPlace.toLocalizedString(resources: Resources) = when (this) {
        STREET -> resources.getString(R.string.quest_address_street_street_name_label)
        PLACE -> resources.getString(R.string.quest_address_street_place_name_label)
    }
}
