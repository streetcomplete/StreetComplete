package de.westnordost.streetcomplete.quests.construction

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

open class MarkCompletedBuildingConstruction(private val overpass: OverpassMapDataAndGeometryApi)
    : OsmElementQuestType<Boolean> {

    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:building=construction"
    override val icon = R.drawable.ic_quest_building_construction

    override fun getTitle(tags: Map<String, String>) = R.string.quest_construction_building_title

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpass.query(getOverpassQuery(bbox), handler)
    }

    /** @return overpass query string to get buildings marked as under construction but excluding ones
     * - with tagged opening date that is in future
     * - recently edited (includes adding/updating check_date tags)
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val tagFilter = "building = construction"

        return bbox.toGlobalOverpassBBox() + """
            wr[$tagFilter]${isNotInFuture("opening_date")} -> .with_unknown_state;
            wr[$tagFilter]${hasRecentlyBeenEdited(180)} -> .recently_edited;
            (.with_unknown_state; - .recently_edited;);
        """.trimIndent() + "\n" + getQuestPrintStatement()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            val value = changes.getPreviousValue("construction") ?: "yes"
            changes.modify("building", value)
            deleteTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, getCurrentDateString())
        }
    }
}
