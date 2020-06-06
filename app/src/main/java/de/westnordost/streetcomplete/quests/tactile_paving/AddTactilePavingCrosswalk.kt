package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox

class AddTactilePavingCrosswalk(private val overpassMapDataApi: OverpassMapDataAndGeometryApi) : OsmElementQuestType<Boolean> {

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
        changes.add("tactile_paving", if (answer) "yes" else "no")
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + """
        
        way[highway = cycleway][foot !~ '^(yes|designated)$']; node(w) -> .exclusive_cycleway_nodes;
        way[highway][access ~ '^(private|no)${'$'}']; node(w) -> .private_road_nodes;

        node[highway = crossing][!tactile_paving][foot != no] -> .crossings;
        
        ((.crossings; - .private_road_nodes; ); - .exclusive_cycleway_nodes;);
        """.trimIndent() + "\n" +
        getQuestPrintStatement()
}
