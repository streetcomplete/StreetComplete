package de.westnordost.streetcomplete.quests.construction

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.DateUtil
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

open class MarkCompletedBuildingConstruction(private val overpass: OverpassMapDataAndGeometryDao)
    : AMarkCompletedConstruction<Boolean>() {

    override val commitMessage = "Determine whether construction is now completed"
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
        val groupName = ".buildings_under_construction"
        val wayGroupName = groupName + "_ways"
        val relationGroupName = groupName + "_relations"
        return bbox.toGlobalOverpassBBox() + "\n" +
            "way" + getQueryPart("building", wayGroupName, 180) +
            "relation" + getQueryPart("building", relationGroupName, 180) +
            "($wayGroupName; $relationGroupName;);\n" +
            getQuestPrintStatement()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            val constructionValue = changes.getPreviousValue("construction") ?: "yes"
            changes.modify("building", constructionValue)
            removeTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY, DateUtil.getCurrentDateString())
        }
    }
}
