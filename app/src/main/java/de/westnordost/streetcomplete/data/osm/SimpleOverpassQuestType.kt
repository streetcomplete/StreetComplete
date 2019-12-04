package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox

/** Quest type that simply makes a certain overpass query using tag filters and creates quests for
 * every element received  */
abstract class SimpleOverpassQuestType<T>(
    private val overpassServer: OverpassMapDataAndGeometryDao
) : OsmElementQuestType<T> {

    private val filter by lazy { FiltersParser().parse(tagFilters) }

    abstract val tagFilters: String

    fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + filter.toOverpassQLString() + getQuestPrintStatement()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassServer.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
