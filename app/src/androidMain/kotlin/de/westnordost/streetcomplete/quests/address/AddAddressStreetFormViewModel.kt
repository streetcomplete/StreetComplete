package de.westnordost.streetcomplete.quests.address

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS

class AddAddressStreetFormViewModel(
    private val nameSuggestionsSource: NameSuggestionsSource
) : ViewModel() {

    private val roadsWithNamesFilter by lazy {
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()
    }

    /** Get the name of the street at the given position, or null if nothing is found */
    fun getNameSuggestionAt(position: LatLon, clickAreaSizeInMeters: Double): String? {
        return nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.find { it.languageTag.isEmpty() }
            ?.name
    }
}
