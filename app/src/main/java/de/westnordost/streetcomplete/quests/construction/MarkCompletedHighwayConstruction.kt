package de.westnordost.streetcomplete.quests.construction

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

open class MarkCompletedHighwayConstruction(private val overpass: OverpassMapDataAndGeometryApi)
    : OsmElementQuestType<Boolean> {

    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:highway=construction"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val isRoad = ALL_ROADS.contains(tags["construction"])
        val isCycleway = tags["construction"] == "cycleway"
        val isFootway = tags["construction"] == "footway"

        return when {
            isRoad -> R.string.quest_construction_road_title
            isCycleway -> R.string.quest_construction_cycleway_title
            isFootway -> R.string.quest_construction_footway_title
            else -> R.string.quest_construction_generic_title
        }
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpass.query(getOverpassQuery(bbox), handler)
    }

    /** @return overpass query string to get streets marked as under construction but excluding ones
     * - with invalid construction tag
     * - with tagged opening date that is in future
     * - recently edited (includes adding/updating check_date tags)
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val tagFilter = "highway = construction"

        return bbox.toGlobalOverpassBBox() + """
            way[$tagFilter]${isNotInFuture("opening_date")} -> .with_unknown_state;
            way[$tagFilter]${hasRecentlyBeenEdited(14)} -> .recently_edited;
            (.with_unknown_state; - .recently_edited;);
        """.trimIndent() + "\n" + getQuestPrintStatement()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            val value = changes.getPreviousValue("construction") ?: "road"
            changes.modify("highway", value)
            deleteTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, getCurrentDateString())
        }
    }
}
