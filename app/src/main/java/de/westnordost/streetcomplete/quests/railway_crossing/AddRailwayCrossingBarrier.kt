package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddRailwayCrossingBarrier(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<String> {

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
        changes.updateWithCheckDate("crossing:barrier", answer)
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        
        way[highway][access ~ '^(private|no)$']; node(w) -> .private_road_nodes;
        way[railway ~ '^(tram|abandoned)$']; node(w) -> .excluded_railways_nodes;
        (.private_road_nodes; .excluded_railways_nodes;) -> .excluded;
        
        node[railway = level_crossing] -> .crossings;

        node.crossings[!'crossing:barrier'] -> .crossings_with_unknown_barrier;
        node.crossings${olderThan(8).toOverpassQLString()} -> .crossings_with_old_barrier;

        ((.crossings_with_unknown_barrier; .crossings_with_old_barrier;); - .excluded;);
        
        ${getQuestPrintStatement()}
        """.trimIndent()

    private fun olderThan(years: Int) =
        TagOlderThan("crossing:barrier", RelativeDate(-(r * 365 * years).toFloat()))

}
