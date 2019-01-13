package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil
import de.westnordost.osmapi.map.data.BoundingBox

/** Quest type that simply makes a certain overpass query using tag filters and creates quests for
 * every element received  */
abstract class SimpleOverpassQuestType<T>(
    private val overpassServer: OverpassMapDataDao) : OsmElementQuestType<T> {

    private val filter by lazy { FiltersParser().parse(tagFilters) }

    protected abstract val tagFilters: String

    fun getOverpassQuery(bbox: BoundingBox) =
        filter.toOverpassQLString(bbox) + OverpassQLUtil.getQuestPrintStatement()

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
