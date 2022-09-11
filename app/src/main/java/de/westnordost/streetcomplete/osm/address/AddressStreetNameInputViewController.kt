package de.westnordost.streetcomplete.osm.address

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.controller.AutoCorrectAbbreviationsViewController
import java.util.Locale

/** Manages inputting a street name associated with an address. The user can either select it via a
 *  coordinate (see [selectStreetAt]) or by typing a name. If the name is an abbreviation, it is
 *  automatically expanded, e.g. "Main st" becomes "Main street".
 *
 *  When selecting the street via a coordinate, the street name is shown in the user's locale if
 *  available in the UI (e.g. `name:en`) to mimic how the street is displayed on the map, but the
 *  default name (`name`) will be returned from [streetName]. */
class AddressStreetNameInputViewController(
    private val streetNameInput: EditText,
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource,
    abbreviationsByLocale: AbbreviationsByLocale,
    private val countryLocale: Locale
) {
    private val autoCorrectAbbreviationsViewController: AutoCorrectAbbreviationsViewController
    private var selectedStreetName: String? = null

    var onInputChanged: (() -> Unit)? = null

    var streetName: String?
        set(value) {
            selectedStreetName = null
            streetNameInput.setText(value)
        }
        get() = selectedStreetName ?: streetNameInput.nonBlankTextOrNull

    init {
        autoCorrectAbbreviationsViewController = AutoCorrectAbbreviationsViewController(streetNameInput)
        autoCorrectAbbreviationsViewController.abbreviations = abbreviationsByLocale[countryLocale]

        streetNameInput.doAfterTextChanged {
            // if the user changed the text, it is now his custom input
            selectedStreetName = null
            onInputChanged?.invoke()
        }
    }

    /** select the name of the street near the given [position] (ast most [radiusInMeters] from it)
     *  instead of typing it in the edit text */
    fun selectStreetAt(position: LatLon, radiusInMeters: Double): Boolean {
        val dist = radiusInMeters + 5
        val namesByLocale = roadNameSuggestionsSource.getNames(listOf(position), dist).firstOrNull()
            ?: return false
        // why using .keys.firstOrNull { Locale(it).language == XXX } instead of .containsKey(XXX):
        // ISO 639 is an unstable standard. For example, id == in. If the comparisons are made
        // with the Locale class, that takes care of it
        val defaultName = namesByLocale[""]
        if (defaultName != null) {
            // name=A -> name=A, name:de=A (in Germany)
            if (namesByLocale.keys.firstOrNull { Locale(it).language == countryLocale.language } == null) {
                namesByLocale[countryLocale.language] = defaultName
            }
        }

        // if available, display the selected street name in the user's locale
        val userLanguage = Locale.getDefault().language
        val lang = namesByLocale.keys.firstOrNull { Locale(it).language == userLanguage }
        if (lang != null) {
            streetNameInput.setText(namesByLocale[lang])
        } else {
            streetNameInput.setText(namesByLocale[""])
        }
        selectedStreetName = namesByLocale[""]
        onInputChanged?.invoke()

        return true
    }
}
