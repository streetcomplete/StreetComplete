package de.westnordost.streetcomplete.quests.bikeway

import android.os.Bundle

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

class AddCycleway(private val overpassServer: OverpassMapDataDao) : OsmElementQuestType {

    override val commitMessage = "Add whether there are cycleways"
    override val icon = R.drawable.ic_quest_bicycleway

    // See overview here: https://ent8r.github.io/blacklistr/?java=bikeway/AddCycleway.java
    // #749. sources:
    // Google Street View (driving around in virtual car)
    // https://en.wikivoyage.org/wiki/Cycling
    // http://peopleforbikes.org/get-local/ (US)
    override val enabledForCountries = Countries.noneExcept(arrayOf(
        // all of Northern and Western Europe, most of Central Europe, some of Southern Europe
        "NO","SE","FI","IS","DK",
        "GB","IE","NL","BE","FR","LU",
        "DE","PL","CZ","HU","AT","CH","LI",
        "ES","IT",
        // East Asia
        "JP","KR","TW",
        // some of China (East Coast)
        "CN-BJ","CN-TJ","CN-SD","CN-JS","CN-SH",
        "CN-ZJ","CN-FJ","CN-GD","CN-CQ",
        // Australia etc
        "NZ","AU",
        // some of Canada
        "CA-BC","CA-QC","CA-ON","CA-NS","CA-PE",
        // some of the US
        // West Coast, East Coast, Center, South
        "US-WA","US-OR","US-CA",
        "US-MA","US-NJ","US-NY","US-DC","US-CT","US-FL",
        "US-MN","US-MI","US-IL","US-WI","US-IN",
        "US-AZ","US-TX"
    ))

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cycleway_title2

    override fun isApplicableTo(element: Element) = null

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler)
    }

    /** @return overpass query string to get streets without cycleway info not near paths for
     * bicycles.
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        val minDistToCycleways = 15 //m

        return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
            "way[highway ~ \"^(primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified)$\"]" +
            "[area != yes]" +
            // not any motorroads
            "[motorroad != yes]" +
            // only without cycleway tags
            "[!cycleway][!\"cycleway:left\"][!\"cycleway:right\"][!\"cycleway:both\"]" +
            "[!\"sidewalk:bicycle\"][!\"sidewalk:both:bicycle\"][!\"sidewalk:left:bicycle\"][!\"sidewalk:right:bicycle\"]" +
            // not any with low speed limit because they not very likely to have cycleway infrastructure
            "[maxspeed !~ \"^(20|15|10|8|7|6|5|10 mph|5 mph|walk)$\"]" +
            // not any unpaved because of the same reason
            "[surface !~ \"^(" + OsmTaggings.ANYTHING_UNPAVED.joinToString("|") + ")$\"]" +
            // not any explicitly tagged as no bicycles
            "[bicycle != no]" +
            "[access !~ \"^private|no$\"]" +
            // some roads may be father than MIN_DIST_TO_CYCLEWAYS from cycleways,
            // not tagged cycleway=separate/sidepath but may have hint that there is
            // a separately tagged cycleway
            "[bicycle != use_sidepath][\"bicycle:backward\" != use_sidepath]" +
            "[\"bicycle:forward\" != use_sidepath]" +
            " -> .streets;" +
            "(" +
            "way[highway=cycleway](around.streets: " + minDistToCycleways + ");" +
            // See #718: If a separate way exists, it may be that the user's answer should
            // correctly be tagged on that separate way and not on the street -> this app would
            // tag data on the wrong elements. So, don't ask at all for separately mapped ways.
            // :-(
            "way[highway ~ \"^(path|footway)$\"](around.streets: " + minDistToCycleways + ");" +
            ") -> .cycleways;" +
            "way.streets(around.cycleways: " + minDistToCycleways + ") -> .streets_near_cycleways;" +
            "(.streets; - .streets_near_cycleways;);" +
            OverpassQLUtil.getQuestPrintStatement()
    }


    override fun createForm()= AddCyclewayForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val right = answer.getString(AddCyclewayForm.CYCLEWAY_RIGHT)
        val left = answer.getString(AddCyclewayForm.CYCLEWAY_LEFT)

        val cyclewayRight = if (right != null) Cycleway.valueOf(right) else null
        val cyclewayLeft = if (left != null) Cycleway.valueOf(left) else null

        val cyclewayRightDir = answer.getInt(AddCyclewayForm.CYCLEWAY_RIGHT_DIR)
        val cyclewayLeftDir = answer.getInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR)

        val bothSidesAreSame = (cyclewayLeft == cyclewayRight && cyclewayLeft != null
                && cyclewayRightDir == 0 && cyclewayLeftDir == 0)

        if (bothSidesAreSame) {
            applyCyclewayAnswerTo(cyclewayLeft!!, Side.BOTH, 0, changes)
        } else {
            if (cyclewayLeft != null) {
                applyCyclewayAnswerTo(cyclewayLeft, Side.LEFT, cyclewayLeftDir, changes)
            }
            if (cyclewayRight != null) {
                applyCyclewayAnswerTo(cyclewayRight, Side.RIGHT, cyclewayRightDir, changes)
            }
        }

        applySidewalkAnswerTo(cyclewayLeft, cyclewayRight, changes)

        if (answer.getBoolean(AddCyclewayForm.IS_ONEWAY_NOT_FOR_CYCLISTS)) {
            changes.addOrModify("oneway:bicycle", "no")
        }
    }

    private fun applySidewalkAnswerTo( cyclewayLeft: Cycleway?, cyclewayRight: Cycleway?,
                                       changes: StringMapChangesBuilder ) {

        val hasSidewalkLeft = cyclewayLeft != null && cyclewayLeft.isOnSidewalk
        val hasSidewalkRight = cyclewayRight != null && cyclewayRight.isOnSidewalk

        val side = when {
            hasSidewalkLeft && hasSidewalkRight -> Side.BOTH
            hasSidewalkLeft -> Side.LEFT
            hasSidewalkRight -> Side.RIGHT
            else -> null
        }

        if (side != null) {
            changes.addOrModify("sidewalk", side.value)
        }
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun applyCyclewayAnswerTo(cycleway: Cycleway, side: Side, dir: Int,
                                      changes: StringMapChangesBuilder ) {
        val directionValue = when {
            dir > 0 -> "yes"
            dir < 0 -> "-1"
            else -> null
        }

        val cyclewayKey = "cycleway:" + side.value
        when (cycleway) {
            NONE, NONE_NO_ONEWAY -> {
                changes.add(cyclewayKey, "no")
            }
            EXCLUSIVE_LANE, ADVISORY_LANE, LANE_UNSPECIFIED -> {
                changes.add(cyclewayKey, "lane")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
                if (cycleway == EXCLUSIVE_LANE)
                    changes.addOrModify("$cyclewayKey:lane", "exclusive")
                else if (cycleway == ADVISORY_LANE)
                    changes.addOrModify("$cyclewayKey:lane","advisory")
            }
            TRACK -> {
                changes.add(cyclewayKey, "track")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
            }
            DUAL_TRACK -> {
                changes.add(cyclewayKey, "track")
                changes.addOrModify("$cyclewayKey:oneway", "no")
            }
            DUAL_LANE -> {
                changes.add(cyclewayKey, "lane")
                changes.addOrModify("$cyclewayKey:oneway", "no")
                changes.addOrModify("$cyclewayKey:lane", "exclusive")
            }
            SIDEWALK_EXPLICIT -> {
                // https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
                changes.add(cyclewayKey, "track")
                changes.add("$cyclewayKey:segregated", "no")
            }
            SIDEWALK_OK -> {
                // https://wiki.openstreetmap.org/wiki/File:Z239Z1022-10GehwegRadfahrerFrei.jpeg
                changes.add(cyclewayKey, "no")
                changes.add("sidewalk:" + side.value + ":bicycle", "yes")
            }
            PICTOGRAMS -> {
                changes.add(cyclewayKey, "shared_lane")
                changes.add("$cyclewayKey:lane", "pictogram")
            }
            SUGGESTION_LANE -> {
                changes.add(cyclewayKey, "shared_lane")
                changes.add("$cyclewayKey:lane", "advisory")
            }
            BUSWAY -> {
                changes.add(cyclewayKey, "share_busway")
            }
        }
    }
}
