package de.westnordost.streetcomplete.quests.kerb_height

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

class AddKerbHeight(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<String> {

    override val commitMessage = "Add kerb height info"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_railway // TODO icon

    override fun getTitle(tags: Map<String, String>) = R.string.quest_kerb_height_title

    override fun createForm() = AddKerbHeightForm()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer)
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}

        way["barrier"="kerb"];
        node(w)->.kerb_way_nodes;
        way["highway"~"^footway|path|cycleway${'$'}"];
        node(w)->.footway_way_nodes;
        (
            node.kerb_way_nodes.footway_way_nodes;
            node.footway_way_nodes["barrier"="kerb"];
        ) -> .kerbs;

        way[highway = cycleway][foot !~ '^(yes|designated)${'$'}']; node(w) -> .exclusive_cycleway_nodes;
        way[highway][access ~ '^(private|no)${'$'}']; node(w) -> .private_road_nodes;
        (.exclusive_cycleway_nodes; .private_road_nodes;) -> .excluded;

        (
            node.kerbs[!kerb];
            node.kerbs[kerb~"^yes|unknown$"];
        ) -> .unknown_state;
        node.kerbs[kerb!=no][kerb!=rolled]${olderThan(8).toOverpassQLString()} -> .outdated_data;

        (
            (
                .unknown_state;
                .outdated_data;
            );
        - .excluded;
        );

        ${getQuestPrintStatement()}
        """.trimIndent()

    private fun olderThan(years: Int) =
        TagOlderThan("tactile_paving", RelativeDate(-(r * 365 * years).toFloat()))

}
