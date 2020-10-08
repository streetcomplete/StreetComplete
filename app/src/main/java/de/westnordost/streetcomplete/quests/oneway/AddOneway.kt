package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ElementFiltersParser
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.*
import de.westnordost.streetcomplete.quests.parking_lanes.*

class AddOneway(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi
) : OsmElementQuestType<OnewayAnswer> {

    private val tagFilters = """
        ways with highway ~ residential|service|tertiary|unclassified
         and !oneway and area != yes and junction != roundabout 
         and (access !~ private|no or (foot and foot !~ private|no))
         and lanes <= 1 and width
    """
    /* should filter out dead end streets, but how? */

    override val commitMessage = "Add whether this road is a one-way road because it is quite slim"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    private val filter by lazy { ElementFiltersParser().parse(tagFilters) }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_oneway2_title

    override fun isApplicableTo(element: Element): Boolean {
        val tags = element.tags ?: return false

        // check if the width of the road minus the space consumed by parking lanes is quite slim
        val width = tags["width"]?.toFloatOrNull()
        if (width != null) {
            if (width > estimateWidthConsumedByParkingLanes(tags) + 4f) return false
        }

        return filter.matches(element)
    }

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        val query = bbox.toGlobalOverpassBBox() + "\n" + filter.toOverpassQLString() + getQuestPrintStatement()
        return overpassMapDataApi.query(query) { element, geometry ->
            if (isApplicableTo(element)) handler(element, geometry)
        }
    }

    private fun estimateWidthConsumedByParkingLanes(tags: Map<String, String>): Float {
        val sides = createParkingLaneSides(tags) ?: return 0f
        return (sides.left?.estimatedWidth ?: 0f) + (sides.right?.estimatedWidth ?: 0f)
    }

    override fun createForm() = AddOnewayForm()

    override fun applyAnswerTo(answer: OnewayAnswer, changes: StringMapChangesBuilder) {
        changes.add("oneway", when(answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        })
    }
}
