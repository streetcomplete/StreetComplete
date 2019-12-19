package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox

class AddSidewalk(private val overpassServer: OverpassMapDataAndGeometryDao) : OsmElementQuestType<SidewalkAnswer> {

    override val commitMessage = "Add whether there are sidewalks"
    override val icon = R.drawable.ic_quest_sidewalk
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_title

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassServer.query(getOverpassQuery(bbox), handler)
    }

    /** returns overpass query string to get streets without sidewalk info not near separately mapped
     *  sidewalks (and other paths)
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val minDistToWays = 15 //m

        // note: this query is very similar to the query in AddCycleway
        return bbox.toGlobalOverpassBBox() + "\n" +
            "way[highway ~ '^(primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential)$']" +
            "[area != yes]" +
            // not any motorroads
            "[motorroad != yes]" +
            // only without sidewalk tags
            "[!sidewalk][!'sidewalk:left'][!'sidewalk:right'][!'sidewalk:both']" +
            // not any with very low speed limit because they not very likely to have sidewalks
            "[maxspeed !~ '^(8|7|6|5|5 mph|walk)$']" +
            // not any unpaved because of the same reason
            "[surface !~ '^(" + OsmTaggings.ANYTHING_UNPAVED.joinToString("|") + ")$']" +
            "[lit = yes]" +
            // not any explicitly tagged as no pedestrians
            "[foot != no]" +
            "[access !~ '^(private|no)$']" +
            // some roads may be farther than minDistToWays from ways, not tagged with
            // footway=separate/sidepath but may have a hint that there is a separately tagged
            // sidewalk
            "[foot != use_sidepath]" +
            " -> .streets;\n" +
            "way[highway ~ '^(path|footway|cycleway)$'](around.streets: " + minDistToWays + ")" +
            " -> .ways;\n" +
            "way.streets(around.ways: " + minDistToWays + ") -> .streets_near_ways;\n" +
            "(.streets; - .streets_near_ways;);\n" +
            getQuestPrintStatement()
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: SidewalkAnswer, changes: StringMapChangesBuilder) {
        changes.add("sidewalk", getSidewalkValue(answer))
    }

    private fun getSidewalkValue(answer: SidewalkAnswer) = when {
        answer.left && answer.right -> "both"
        answer.left ->  "left"
        answer.right ->  "right"
        else -> "none"
    }
}
