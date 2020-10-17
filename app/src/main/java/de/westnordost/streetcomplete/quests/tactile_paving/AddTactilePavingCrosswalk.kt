package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTactilePavingCrosswalk(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<Boolean> {

    override val commitMessage = "Add tactile pavings on crosswalks"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_blind_pedestrian_crossing

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=tactile_paving/AddTactilePavingCrosswalk.kt
    // #750
    override val enabledInCountries = NoCountriesExcept(
            // Europe
            "NO", "SE",
            "GB", "IE", "NL", "BE", "FR", "ES",
            "DE", "PL", "CZ", "SK", "HU", "AT", "CH",
            "LV", "LT", "EE", "RU",
            // America
            "US", "CA", "AR",
            // Asia
            "HK", "SG", "KR", "JP",
            // Oceania
            "AU", "NZ"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_crosswalk

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("tactile_paving", answer.toYesNo())
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        
        way[highway = cycleway][foot !~ '^(yes|designated)$']; node(w) -> .exclusive_cycleway_nodes;
        way[highway][access ~ '^(private|no)$']; node(w) -> .private_road_nodes;
        (.exclusive_cycleway_nodes; .private_road_nodes;) -> .excluded;

        (
            node[highway = traffic_signals][crossing = traffic_signals][foot != no];
            node[highway = crossing][foot != no];
        ) -> .crossings;
        
        node.crossings[!tactile_paving] -> .crossings_with_unknown_tactile_paving;
        node.crossings[tactile_paving = no]${olderThan(4).toOverpassQLString()} -> .old_crossings_without_tactile_paving;
        node.crossings${olderThan(8).toOverpassQLString()} -> .very_old_crossings;
        
        (
            (
                .crossings_with_unknown_tactile_paving;
                .old_crossings_without_tactile_paving;
                .very_old_crossings;
            ); 
        - .excluded;
        );
        
        ${getQuestPrintStatement()}
        """.trimIndent()

    private fun olderThan(years: Int) =
        TagOlderThan("tactile_paving", RelativeDate(-(r * 365 * years).toFloat()))
}
