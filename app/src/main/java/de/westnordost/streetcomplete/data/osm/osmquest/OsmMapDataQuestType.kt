package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element

/** Quest type based on OSM data whose quests can be created by looking at a MapData */
interface OsmMapDataQuestType<T> : OsmElementQuestType<T> {
    /** return all elements within the given map data that are applicable to this quest type. */
    fun getApplicableElements(mapData: MapDataWithGeometry): List<Element>
}