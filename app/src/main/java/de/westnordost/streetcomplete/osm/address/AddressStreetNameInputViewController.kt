package de.westnordost.streetcomplete.osm.address

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.quests.NameSuggestionsSource
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.controller.AutoCorrectAbbreviationsViewController
import java.util.Locale

/** Manages inputting a street name associated with an address. The user can either select it via a
 *  coordinate (see [selectStreetAt]) or by typing a name. If the name is an abbreviation, it is
 *  automatically expanded, e.g. "Main st" becomes "Main street" */
class AddressStreetNameInputViewController(
    private val streetNameInput: EditText,
    private val nameSuggestionsSource: NameSuggestionsSource,
    abbreviationsByLocale: AbbreviationsByLocale,
    private val countryLocale: Locale
) {
    private val autoCorrectAbbreviationsViewController: AutoCorrectAbbreviationsViewController

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    var onInputChanged: (() -> Unit)? = null

    var streetName: String?
        set(value) { streetNameInput.setText(value) }
        get() = streetNameInput.nonBlankTextOrNull

    init {
        autoCorrectAbbreviationsViewController = AutoCorrectAbbreviationsViewController(streetNameInput)
        autoCorrectAbbreviationsViewController.abbreviations = abbreviationsByLocale[countryLocale]

        streetNameInput.doAfterTextChanged { onInputChanged?.invoke() }
    }

    /** select the name of the street near the given [position] (at most [radiusInMeters] from it)
     *  instead of typing it in the edit text */
    fun selectStreetAt(position: LatLon, radiusInMeters: Double): Boolean {
        val namesByLocale = nameSuggestionsSource
            .getNames(
                points = listOf(position),
                maxDistance = radiusInMeters,
                filter = roadsWithNamesFilter
            )
            .firstOrNull()
            ?.associate { it.languageTag to it.name }?.toMutableMap()
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
        streetNameInput.setText(namesByLocale[""])

        return true
    }
}
