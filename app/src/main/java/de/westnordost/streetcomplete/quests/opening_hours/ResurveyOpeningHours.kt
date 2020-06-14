package de.westnordost.streetcomplete.quests.opening_hours

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.construction.getCurrentDateString
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.FutureTask

class ResurveyOpeningHours(
        private val overpass: OverpassMapDataAndGeometryApi,
        featureDictionaryFuture: FutureTask<FeatureDictionary>,
        private val parser: OpeningHoursTagParser
) : OpeningHours(featureDictionaryFuture) {
    override val commitMessage = "resurvey opening hours"
    override val icon = R.drawable.ic_quest_opening_hours
    override fun getTitle(tags: Map<String, String>) =
            if (hasProperName(tags))
                R.string.resurvey_opening_hours_title
            else
                R.string.resurvey_opening_hours_no_name_title

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpass.query(getOverpassQuery(bbox)) { element, geometry ->
            // only show places that can be named somehow
            if (hasName(element.tags)) {
                if (parser.parse(element.tags["opening_hours"]!!) != null) {
                    handler(element, geometry)
                } else {
                    Log.wtf("AAAA", "opening_hours=" + element.tags["opening_hours"] + " was rejected as not representable in SC")
                }
            }
        }
    }

    /** Note that 'newer:' query part will ensure that any edit,
     * including adding or updating review marker like check_date or survey:date tags
     * will cause OSM elements to become ineligible for this quest for reviewIntervalInDays days.
     * It allows supporting check_date and any other survey markers without parsing of any tags.
     * In addition, objects where tag was surveyed and confirmed to be correct by adding check_date:opening_hours
     * are with larger delay, as it means this info on this specific object is relatively stable
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val reviewIntervalInDays = 380
        val resurveyCutoff = "'${getOffsetDateString(-reviewIntervalInDays)}T00:00:00Z'"
        val repeatedResurveyCutoff = "'${getOffsetDateString(-reviewIntervalInDays * 3)}T00:00:00Z'"
        return bbox.toGlobalOverpassBBox() + """
            nwr[opening_hours]['opening_hours:signed'!='no']
                (if:
                    !is_date(t['check_date:opening_hours']) ||
                    date(t['check_date:opening_hours']) < date($repeatedResurveyCutoff)
                ) -> .oh_without_recent_check_date;

            nwr[opening_hours](newer: $resurveyCutoff) -> .recently_edited_objects_with_oh;
            nwr[name!~".*"][noname!=yes][opening_hours] -> .oh_with_unknown_name_state;

            (.oh_without_recent_check_date; - .recently_edited_objects_with_oh;) -> .old_oh;
            (.old_oh; - .oh_with_unknown_name_state;);
            """.trimIndent() + getQuestPrintStatement()
    }

    /** Technically, it is possible to run a check using element.timestamp and element.tags, but...
     * Checking timestamp will fail any check with recent edit - including undo and adding other tags
     * So, implementing isApplicableTo would never cause this quest to appear, including after undos
     * and may it cause to disappear when it would be not desirable.
     *
     * For example POI with name and opening_hours tags, not edited for a long time would stop matching
     * after adding unrelated tags like wheelchair.
     *
     * And not checking timestamp would cause it to appear after, for example, adding opening_hours tag
     */
    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = ResurveyOpeningHoursForm(parser)

    override fun applyAnswerTo(answer: OpeningHoursAnswer, changes: StringMapChangesBuilder) {
        val checkTag = SURVEY_MARK_KEY + ":opening_hours"
        changes.deleteIfExists("opening_hours:lastcheck")
        when (answer) {
            is AlwaysOpen -> {
                changes.deleteIfExists(checkTag)
                // assuming modify is OK as
                // opening_hours=24/7 is never present on any elements qualified for this quest
                changes.modify("opening_hours", "24/7")
            }
            is NoOpeningHoursSign -> {
                changes.add("opening_hours:signed", "no")
            }
            is UnmodifiedOpeningHours -> {
                changes.addOrModify(checkTag, getCurrentDateString())
            }
            is RegularOpeningHours -> {
                changes.deleteIfExists(checkTag)
                changes.modify("opening_hours", parser.internalFlatIntoTag(answer.times))
            }
            else -> throw AssertionError()
        }
    }

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * negative offsetInDays returns dates from past, positive from future
     */
    private fun getOffsetDateStringFromDate(offsetInDays: Int, date: Date): String {
        val modifiedCalendar = Calendar.getInstance()
        modifiedCalendar.time = date
        modifiedCalendar.add(Calendar.DAY_OF_MONTH, offsetInDays)
        return DATE_FORMAT.format(modifiedCalendar.time)
    }

    /**
     * negative offsetInDays returns dates from past, positive from future
     */
    private fun getOffsetDateString(offsetInDays: Int) =
            getOffsetDateStringFromDate(offsetInDays, Calendar.getInstance().time)
}
