package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.NameSuggestionSource

class RoadNameSuggestionsSource(
    private val mapDataSource: MapDataWithEditsSource
) {

    fun getNames(points: List<LatLon>, maxDistance: Double): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        val nameSuggestionSource = NameSuggestionSource(mapDataSource)
        val mapData = nameSuggestionSource.expandedMapData(points, maxDistance)
        val elementFilter = """
            ways with
                highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
                and name
        """

        return nameSuggestionSource.getNames(
            points, maxDistance, mapData, mapData.filter(elementFilter)
        )
    }
}
