package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.NameSuggestionSource

class BusStopNameSuggestionsSource(
    private val mapDataSource: MapDataWithEditsSource
) {

    fun getNames(points: List<LatLon>, maxDistance: Double): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        val nameSuggestionSource = NameSuggestionSource(mapDataSource)
        val mapData = nameSuggestionSource.expandedMapData(points, maxDistance)
        val elementFilter = """
            nodes, ways with
            (
              public_transport = platform and bus = yes
              or (highway = bus_stop and public_transport != stop_position)
              or railway = halt
              or railway = station
              or railway = tram_stop
            )
            and name
        """

        return nameSuggestionSource.getNames(
            points, maxDistance, mapData, mapData.filter(elementFilter)
        )
    }
}
