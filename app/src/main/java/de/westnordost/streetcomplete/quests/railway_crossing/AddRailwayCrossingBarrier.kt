package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox

class AddRailwayCrossingBarrier(private val overpassMapDataApi: OverpassMapDataAndGeometryApi) : OsmElementQuestType<String> {
    override val commitMessage = "Add type of barrier for railway crossing"
    override val wikiLink = "Key:crossing:barrier"
    override val icon = R.drawable.ic_quest_railway

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_crossing_barrier_title

    override fun createForm() = AddRailwayCrossingBarrierForm()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("crossing:barrier", answer)
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + """
        way[highway][access ~ '^(private|no)$'];
        node(w) -> .private_roads;
        way[railway ~ '^(tram|abandoned)$'];
        node(w) -> .excluded_railways;
        node[railway = level_crossing][!'crossing:barrier'];
        (._; - .private_roads; );
        (._; - .excluded_railways; );""".trimIndent() + "\n" +
        getQuestPrintStatement()
}
