package de.westnordost.streetcomplete.quests.opening_hours

import android.util.Log
import ch.poole.openinghoursparser.OpeningHoursParser
import ch.poole.openinghoursparser.ParseException
import ch.poole.openinghoursparser.Rule
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.DateUtil
import java.io.ByteArrayInputStream

class ResurveyOpeningHours (private val overpassServer: OverpassMapDataDao) : OsmElementQuestType<OpeningHoursAnswer> {
    override val commitMessage = "resurvey opening hours"
    override val icon = R.drawable.ic_quest_guidepost
    override fun getTitle(tags: Map<String, String>) = R.string.resurvey_opening_hours_title
    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"]!!
        val openingHours = tags.getValue("opening_hours")
        var parsed = ""
        try {
            val input = ByteArrayInputStream(openingHours.toByteArray())
            val parser = OpeningHoursParser(input)
            val rules: ArrayList<Rule> = parser.rules(false)
            for (rule in rules) {
                parsed += rule.toDebugString()
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            parsed = "parsing failed"
        }
        val displayed = "$openingHours \n\n<$parsed>\n\n"
        return arrayOf(displayed, name)
    }

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox)) { element, geometry ->
            if(element.tags != null) {
                // require opening hours that are supported
                if (OpeningHoursTagParser.parse(element.tags["opening_hours"]!!) != null) {
                    handler.handle(element, geometry)
                } else {
                    Log.wtf("AAAA", "opening_hours=" + element.tags["opening_hours"] + " was rejected as not representable in SC")
                }
            }

        }
    }

    private fun getOverpassQuery(bbox: BoundingBox): String {
        val groupName = ".old_opening_hours"
        val nodeGroupName = groupName + "_nodes"
        val wayGroupName = groupName + "_ways"
        val relationGroupName = groupName + "_relations"
        val reviewIntervalInDays = 380
        return bbox.toGlobalOverpassBBox() + "\n" +
                getQueryPart("node", nodeGroupName, reviewIntervalInDays) +
                getQueryPart("way", wayGroupName, reviewIntervalInDays) +
                getQueryPart("relation", relationGroupName, reviewIntervalInDays) +
                "($nodeGroupName; $wayGroupName; $relationGroupName;);\n" +
                getQuestPrintStatement()
    }

    // Note that newer segment will ensure that any edit,
    // including adding or updating review marker like check_date or survey:date tags
    // will cause OSM elements to become ineligible for this quest for reviewIntervalInDays days.
    // It allows supporting check_date and any other survey markers without parsing of any tags.
    //
    // objects where tag was surveyed and confirmed to be correct by adding check_date tags
    // are with larger delay, as it means this info on this specific object is relatively stable
    protected fun getQueryPart(objectType: String, nameOfGeneratedGroup: String, reviewIntervalInDays: Int) =
        "$objectType[name][opening_hours]['opening_hours:signed'!='no'](if:!is_date(t['check_date:opening_hours']) || date(t['check_date:opening_hours']) < " +
        "date('${DateUtil.getOffsetDateString(-reviewIntervalInDays * 3)}T00:00:00Z')) -> .old_opening_hours_tag;\n" +
        "$objectType[name][opening_hours](newer: '${DateUtil.getOffsetDateString(-reviewIntervalInDays)}T00:00:00Z') -> .recently_edited_objects_with_opening_hours;\n" +
        "(.old_opening_hours_tag; - .recently_edited_objects_with_opening_hours;)-> $nameOfGeneratedGroup;\n"

    override fun isApplicableTo(element: Element) = null

    override fun createForm() = ResurveyOpeningHoursForm()

    override fun applyAnswerTo(answer: OpeningHoursAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is AlwaysOpen -> changes.modify("opening_hours", "24/7")
            is NoOpeningHoursSign -> changes.add("opening_hours:signed", "no")
            is UnmodifiedOpeningHours -> changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY + ":opening_hours", DateUtil.getCurrentDateString())
            is RegularOpeningHours -> changes.add("opening_hours", answer.times.joinToString(";"))
        }
    }
}
