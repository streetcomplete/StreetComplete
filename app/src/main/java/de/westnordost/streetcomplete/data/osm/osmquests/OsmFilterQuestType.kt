package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter

/** Quest type where each quest refers to one OSM element where the element selection is based on
 *  a simple [element filter expression][de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression].
 */
abstract class OsmFilterQuestType<T> : OsmElementQuestType<T> {

    val filter by lazy { elementFilter.toElementFilterExpression() }

    protected abstract val elementFilter: String

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter(elementFilter).asIterable()

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
