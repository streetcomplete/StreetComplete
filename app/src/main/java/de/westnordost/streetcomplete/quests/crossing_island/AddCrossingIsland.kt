package de.westnordost.streetcomplete.quests.crossing_island

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddCrossingIsland(private val overpass: OverpassMapDataAndGeometryApi)
    : OsmElementQuestType<Boolean> {

    override val commitMessage = "Add whether pedestrian crossing has an island"
    override val wikiLink = "Key:crossing:island"
    override val icon = R.drawable.ic_quest_pedestrian_crossing_island

    override fun getTitle(tags: Map<String, String>) = R.string.quest_pedestrian_crossing_island

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpass.query(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox): String =
        bbox.toGlobalOverpassBBox() + """
        node[highway = crossing][crossing][crossing != island][!"crossing:island"] -> .crossings;
        .crossings < -> .crossedWays;
        (
          way.crossedWays[highway][access ~ "^(private|no)$"];
          way.crossedWays[highway][oneway][oneway != no];
          way.crossedWays[highway ~ "^(path|footway|cycleway|pedestrian)$"];
        ) -> .excludedWays;

        ( .crossings; - node(w.excludedWays); );
    """.trimIndent() + "\n" + getQuestPrintStatement()

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("crossing:island", answer.toYesNo())
    }
}
