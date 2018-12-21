package de.westnordost.streetcomplete.quests.construction

import android.os.Bundle

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil
import de.westnordost.streetcomplete.quests.DateUtil
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class MarkCompletedHighwayConstruction(private val overpass: OverpassMapDataDao) : AMarkCompletedConstruction() {

    override val commitMessage = "Determine whether construction is now completed"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val isRoad = OsmTaggings.ALL_ROADS.contains(tags["construction"])
        val isCycleway = tags["construction"] == "cycleway"
        val isFootway = tags["construction"] == "footway"

        return when {
            isRoad -> R.string.quest_construction_road_title
            isCycleway -> R.string.quest_construction_cycleway_title
            isFootway -> R.string.quest_construction_footway_title
            else -> R.string.quest_construction_generic_title
        }
    }

    override fun isApplicableTo(element: Element) = null

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpass.getAndHandleQuota(getOverpassQuery(bbox), handler)
    }

    /** @return overpass query string to get streets marked as under construction but excluding ones
     * - with invalid construction tag
     * - with tagged opening date that is in future
     * - recently edited (includes adding/updating check_date tags)
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val groupName = ".roads_for_review"
        return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
            "way" + getQueryPart("highway", groupName, 14) +
            groupName + " " + OverpassQLUtil.getQuestPrintStatement()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
            val constructionValue = changes.getPreviousValue("construction") ?: "road"
            changes.modify("highway", constructionValue)
            removeTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY, DateUtil.getCurrentDateString())
        }
    }
}
