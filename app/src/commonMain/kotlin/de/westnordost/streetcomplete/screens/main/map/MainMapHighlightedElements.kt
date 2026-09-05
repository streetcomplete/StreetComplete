package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LazyMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.math.enlargedBy

/** Builds the contextual feature markers requested by an open OSM quest. */
internal fun getMainMapHighlightedElementMarkers(
    quest: OsmQuest,
    element: Element,
    mapDataSource: MapDataWithEditsSource,
    featureDictionary: FeatureDictionary,
): List<Marker> {
    val bounds = quest.geometry.bounds.enlargedBy(quest.type.highlightedElementsRadius)
    val mapData = LazyMapDataWithGeometry(bounds, mapDataSource)
    val levels = parseLevelsOrNull(element.tags)

    return quest.type.getHighlightedElements(element, mapData).mapNotNull { highlighted ->
        if (element == highlighted) return@mapNotNull null
        if (!levels.levelsIntersect(parseLevelsOrNull(highlighted.tags))) return@mapNotNull null
        if (element.tags["layer"] != highlighted.tags["layer"]) return@mapNotNull null

        val geometry = mapData.getGeometry(highlighted.type, highlighted.id)
            ?: return@mapNotNull null
        Marker(
            geometry = geometry,
            icon = getIcon(featureDictionary, highlighted),
            title = getTitle(highlighted.tags),
        )
    }.toList()
}
