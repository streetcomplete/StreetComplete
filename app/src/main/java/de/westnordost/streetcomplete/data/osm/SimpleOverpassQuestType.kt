package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil
import de.westnordost.osmapi.map.data.BoundingBox

/** Quest type that simply makes a certain overpass query using tag filters and creates quests for
 * every element received  */
abstract class SimpleOverpassQuestType(
    private val overpassServer: OverpassMapDataDao) : OsmElementQuestType {

    private val filter by lazy { FiltersParser().parse(tagFilters) }

    protected abstract val tagFilters: String

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        val query = filter.toOverpassQLString(bbox) + OverpassQLUtil.getQuestPrintStatement()
        return overpassServer.getAndHandleQuota(query, handler)
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
