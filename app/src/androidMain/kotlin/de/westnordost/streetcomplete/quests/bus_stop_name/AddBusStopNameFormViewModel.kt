package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

class AddBusStopNameFormViewModel(
    private val nameSuggestionsSource: NameSuggestionsSource
) : ViewModel() {

    // this filter needs to be kept somewhat in sync with the filter in AddBusStopName
    private val busStopsWithNamesFilter by lazy { """
        nodes, ways, relations with
        (
          public_transport = platform and bus = yes
          or highway = bus_stop and public_transport != stop_position
          or railway ~ halt|station|tram_stop
        )
        and name
    """.toElementFilterExpression()
    }

    /** Get the name of the bus stop at the given position, or null if nothing is found */
    fun getNamesSuggestionAt(position: LatLon, clickAreaSizeInMeters: Double): List<LocalizedName>? {
        return nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, busStopsWithNamesFilter)
            .firstOrNull()
    }
}
