package de.westnordost.streetcomplete.quests.tactile_paving

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
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrierForm
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTactilePavingKerb(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<Boolean> {

    override val commitMessage = "Add tactile paving on kerbs"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_railway // TODO icon

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactile_paving_kerb

    override fun createForm() = TactilePavingForm()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("tactile_paving", answer.toYesNo())
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}

        way["barrier"="kerb"];
        node(w)->.kerb_way_nodes;
        way["highway"~"^footway|path|cycleway$"];
        node(w)->.footway_way_nodes;
        (
            node.kerb_way_nodes.footway_way_nodes;
            node.footway_way_nodes["barrier"="kerb"];
        ) -> .kerbs;

        way[highway = cycleway][foot !~ '^(yes|designated)$']; node(w) -> .exclusive_cycleway_nodes;
        way[highway][access ~ '^(private|no)$']; node(w) -> .private_road_nodes;
        (.exclusive_cycleway_nodes; .private_road_nodes;) -> .excluded;

        (
            node[highway = traffic_signals][crossing = traffic_signals][foot != no];
            node[highway = crossing][foot != no];
        ) -> .crossings;

        .kerbs[!tactile_paving] -> .unknown_tactile_paving;
        .kerbs[tactile_paving = no]${olderThan(4).toOverpassQLString()} -> .old_without_tactile_paving;
        .kerbs${olderThan(8).toOverpassQLString()} -> .very_old;

        (
            (
                .unknown_tactile_paving;
                .old_without_tactile_paving;
                .very_old;
            );
        - .excluded;
        );

        ${getQuestPrintStatement()}
        """.trimIndent()

    private fun olderThan(years: Int) =
        TagOlderThan("tactile_paving", RelativeDate(-(r * 365 * years).toFloat()))

}
