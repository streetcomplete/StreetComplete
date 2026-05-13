package de.westnordost.streetcomplete.quests.road_name

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

class AddRoadNameFormViewModel(
    private val nameSuggestionsSource: NameSuggestionsSource
) : ViewModel() {

    private val roadsWithNamesFilter by lazy {
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()
    }

    fun getNameSuggestionAt(position: LatLon, clickAreaSizeInMeters: Double): List<LocalizedName>? {
        return nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
    }
}
