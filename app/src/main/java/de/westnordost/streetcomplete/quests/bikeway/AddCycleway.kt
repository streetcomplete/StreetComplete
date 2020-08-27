package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.meta.deleteCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.ktx.containsAny

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddCycleway(
    private val overpassApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<CyclewayAnswer> {

    override val commitMessage = "Add whether there are cycleways"
    override val wikiLink = "Key:cycleway"
    override val icon = R.drawable.ic_quest_bicycleway

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=bikeway/AddCycleway.kt
    // #749. sources:
    // Google Street View (driving around in virtual car)
    // https://en.wikivoyage.org/wiki/Cycling
    // http://peopleforbikes.org/get-local/ (US)
    override val enabledInCountries = NoCountriesExcept(
            // all of Northern and Western Europe, most of Central Europe, some of Southern Europe
            "NO", "SE", "FI", "IS", "DK",
            "GB", "IE", "NL", "BE", "FR", "LU",
            "DE", "PL", "CZ", "HU", "AT", "CH", "LI",
            "ES", "IT",
            // East Asia
            "JP", "KR", "TW",
            // some of China (East Coast)
            "CN-BJ", "CN-TJ", "CN-SD", "CN-JS", "CN-SH",
            "CN-ZJ", "CN-FJ", "CN-GD", "CN-CQ",
            // Australia etc
            "NZ", "AU",
            // some of Canada
            "CA-BC", "CA-QC", "CA-ON", "CA-NS", "CA-PE",
            // some of the US
            // West Coast, East Coast, Center, South
            "US-WA", "US-OR", "US-CA",
            "US-MA", "US-NJ", "US-NY", "US-DC", "US-CT", "US-FL",
            "US-MN", "US-MI", "US-IL", "US-WI", "US-IN",
            "US-AZ", "US-TX"
    )

    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) : Int {
        val isCyclewayTagged = tags.keys.containsAny(KNOWN_CYCLEWAY_KEYS)
        val isCyclewayValueAmbiguous = tags.filterKeys { it in KNOWN_CYCLEWAY_KEYS }.values.any { it in AMBIGIOUS_CYCLEWAY_VALUES }

        return if (isCyclewayTagged && !isCyclewayValueAmbiguous)
            R.string.quest_cycleway_resurvey_title
        else
            R.string.quest_cycleway_title2
    }

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassApi.query(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox): String {
        val minDistToCycleways = 15 //m

        val anythingUnpaved = ANYTHING_UNPAVED.joinToString("|")
        val olderThan8Years = olderThan(8).toOverpassQLString()
        val handledCycleways = HANDLED_CYCLEWAY_VALUES.joinToString("|")
        val handledCyclewayLanes = HANDLED_CYCLEWAY_LANE_VALUES.joinToString("|")

        /* Excluded is
           - anything explicitly tagged as no bicycles or having to use separately mapped sidepath
           - if not already tagged with a cycleway: streets with low speed or that are not paved, as
             they are very unlikely to have cycleway infrastructure
           - if not already tagged, roads that are close (15m) to foot or cycleways (see #718)
           - if already tagged, if not older than 8 years or if the cycleway tag uses some unknown value
         */
        return bbox.toGlobalOverpassBBox() + """
            way
                [highway ~ '^(primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified)$']
                [motorroad != yes]
                [bicycle_road != yes][cyclestreet != yes]
                [area != yes]
                [bicycle != no][bicycle != designated]
                [access !~ '^(private|no)$']
                [bicycle != use_sidepath]
                ['bicycle:backward' != use_sidepath]['bicycle:forward' != use_sidepath]
            -> .streets;
            
            way.streets
                [!cycleway]
                [!'cycleway:left'][!'cycleway:right'][!'cycleway:both']
                [!'sidewalk:bicycle']
                [!'sidewalk:both:bicycle'][!'sidewalk:left:bicycle'][!'sidewalk:right:bicycle']
                [maxspeed !~ '^(20|15|10|8|7|6|5|10 mph|5 mph|walk)$']
                [surface !~ '^($anythingUnpaved)$']
            -> .untagged;
            way[highway ~ '^(path|footway|cycleway)$'](around.streets: $minDistToCycleways) -> .cycleways;
            way.untagged(around.cycleways: $minDistToCycleways) -> .untagged_near_cycleways;
            
            way.streets
                [~'^(cycleway(:(left|right|both))?)$']
                $olderThan8Years
            -> .old;
            
            (""" +
                KNOWN_CYCLEWAY_KEYS.map { "way.old['$it']['$it' !~ '^($handledCycleways)$'];\n" } +
                KNOWN_CYCLEWAY_LANES_KEYS.map { "way.old['$it']['$it' !~ '^($handledCyclewayLanes)$'];\n" } +
            """) -> .old_with_unknown_tags;

            (
                (.untagged; - .untagged_near_cycleways;);
                (old; .old_with_unknown_tags;);
            );
            
            ${getQuestPrintStatement()}
            """.trimIndent()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        val tags = element.tags ?: return false
        // can't determine for yet untagged roads by the tags alone because we need info about
        // surrounding geometry, but for already tagged ones, we can!
        return tags.keys.containsAny(KNOWN_CYCLEWAY_KEYS)
                && olderThan(8).matches(element)
                && tags.filterKeys { it in KNOWN_CYCLEWAY_KEYS }.values.all { it in HANDLED_CYCLEWAY_VALUES }
                && tags.filterKeys { it in KNOWN_CYCLEWAY_LANE_VALUES }.values.all { it in HANDLED_CYCLEWAY_LANE_VALUES }
    }

    private fun olderThan(years: Int) =
        TagOlderThan("cycleway", RelativeDate(-(r * 365 * years).toFloat()))

    override fun createForm() = AddCyclewayForm()

    override fun applyAnswerTo(answer: CyclewayAnswer, changes: StringMapChangesBuilder) {

        if (answer.left == answer.right) {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.BOTH, 0, changes) }
            deleteCyclewayAnswerIfExists(Side.LEFT, changes)
            deleteCyclewayAnswerIfExists(Side.RIGHT, changes)
        } else {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.LEFT, it.dirInOneway, changes) }
            answer.right?.let { applyCyclewayAnswerTo(it.cycleway, Side.RIGHT, it.dirInOneway, changes) }
            deleteCyclewayAnswerIfExists(Side.BOTH, changes)
        }
        deleteCyclewayAnswerIfExists(null, changes)

        applySidewalkAnswerTo(answer.left?.cycleway, answer.right?.cycleway, changes)

        if (answer.isOnewayNotForCyclists) {
            changes.addOrModify("oneway:bicycle", "no")
        } else {
            changes.deleteIfPreviously("oneway:bicycle", "no")
        }

        // only set the check date if nothing was changed
        val isNotActuallyChangingAnything = changes.getChanges().all { change ->
            change is StringMapEntryModify && change.value == change.valueBefore
        }
        if (isNotActuallyChangingAnything) {
            changes.updateCheckDateForKey("cycleway")
        } else {
            changes.deleteCheckDatesForKey("cycleway")
        }
    }

    /** Just add a sidewalk if we implicitly know from the answer that there is one */
    private fun applySidewalkAnswerTo(
        cyclewayLeft: Cycleway?, cyclewayRight: Cycleway?, changes: StringMapChangesBuilder ) {

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
                changes.addOrModify(cyclewayKey, "no")
            }
            EXCLUSIVE_LANE, ADVISORY_LANE, LANE_UNSPECIFIED -> {
                changes.addOrModify(cyclewayKey, "lane")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
                if (cycleway == EXCLUSIVE_LANE)
                    changes.addOrModify("$cyclewayKey:lane", "exclusive")
                else if (cycleway == ADVISORY_LANE)
                    changes.addOrModify("$cyclewayKey:lane","advisory")
            }
            TRACK -> {
                changes.addOrModify(cyclewayKey, "track")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
            }
            DUAL_TRACK -> {
                changes.addOrModify(cyclewayKey, "track")
                changes.addOrModify("$cyclewayKey:oneway", "no")
            }
            DUAL_LANE -> {
                changes.addOrModify(cyclewayKey, "lane")
                changes.addOrModify("$cyclewayKey:oneway", "no")
                changes.addOrModify("$cyclewayKey:lane", "exclusive")
            }
            SIDEWALK_EXPLICIT -> {
                // https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
                changes.addOrModify(cyclewayKey, "track")
                changes.addOrModify("$cyclewayKey:segregated", "no")
            }
            SIDEWALK_OK -> {
                // https://wiki.openstreetmap.org/wiki/File:Z239Z1022-10GehwegRadfahrerFrei.jpeg
                changes.addOrModify(cyclewayKey, "no")
                changes.addOrModify("sidewalk:" + side.value + ":bicycle", "yes")
            }
            PICTOGRAMS -> {
                changes.addOrModify(cyclewayKey, "shared_lane")
                changes.addOrModify("$cyclewayKey:lane", "pictogram")
            }
            SUGGESTION_LANE -> {
                changes.addOrModify(cyclewayKey, "shared_lane")
                changes.addOrModify("$cyclewayKey:lane", "advisory")
            }
            BUSWAY -> {
                changes.addOrModify(cyclewayKey, "share_busway")
            }
        }

        // clear previous cycleway:lane value
        if (!cycleway.isLane) {
            changes.deleteIfExists("$cyclewayKey:lane")
        }
        // clear previous cycleway:oneway=no value (if not about to set a new value)
        if (!cycleway.isOneway && directionValue == null) {
            changes.deleteIfPreviously("$cyclewayKey:oneway", "no")
        }
        // clear previous cycleway:segregated=no value
        if (cycleway != SIDEWALK_EXPLICIT) {
            changes.deleteIfPreviously("$cyclewayKey:segregated", "no")
        }
        // clear previous sidewalk:bicycle=yes value
        if (cycleway != SIDEWALK_OK) {
            changes.deleteIfPreviously("sidewalk:" + side.value + ":bicycle", "yes")
        }
    }

    /** clear previous answers for the given side */
    private fun deleteCyclewayAnswerIfExists(side: Side?, changes: StringMapChangesBuilder) {
        val sideVal = if (side == null) "" else ":" + side.value
        val cyclewayKey = "cycleway$sideVal"

        // only things are cleared that are set by this quest
        // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
        changes.deleteIfExists(cyclewayKey)
        changes.deleteIfExists("$cyclewayKey:lane")
        changes.deleteIfExists("$cyclewayKey:segregated")
        changes.deleteIfExists("sidewalk$sideVal:bicycle")
    }

    companion object {
        private val KNOWN_CYCLEWAY_KEYS = listOf(
            "cycleway", "cycleway:left", "cycleway:right", "cycleway:both"
        )
        private val KNOWN_CYCLEWAY_LANES_KEYS = listOf(
            "cycleway:lane", "cycleway:left:lane", "cycleway:right:lane", "cycleway:both:lane"
        )

        private val AMBIGIOUS_CYCLEWAY_VALUES = listOf(
            "yes",   // unclear what type
            "left",  // unclear what type; wrong tagging scheme (sidewalk=left)
            "right", // unclear what type; wrong tagging scheme
            "both",  // unclear what type; wrong tagging scheme
            "shared" // unclear if it is shared_lane or share_busway (or shared with pedestrians)
        )
        private val KNOWN_CYCLEWAY_VALUES = listOf(
            "lane",
            "track",
            "shared_lane",
            "share_busway",
            "no",
            "none",
            // TODO handle these when retagging? See what cyclosm-people have to say https://github.com/cyclosm/cyclosm-cartocss-style/issues/426
            "opposite_lane",         // synonymous for oneway:bicycle=no + cycleway=lane
            "opposite_track",        // synonymous for oneway:bicycle=no + cycleway=track
            "opposite_share_busway", // synonymous for oneway:bicycle=no + cycleway=share_busway
            "opposite"               // synonymous for oneway:bicycle=no + cycleway=no
        )

        private val KNOWN_CYCLEWAY_LANE_VALUES = listOf(
            "exclusive",
            "advisory",
            "pictogram",
            "mandatory", "exclusive_lane",          // same as exclusive. Exclusive lanes are mandatory for bicyclists
            "soft_lane", "advisory_lane", "dashed"  // synonym for advisory lane
        )


        private val HANDLED_CYCLEWAY_VALUES =
            AMBIGIOUS_CYCLEWAY_VALUES + KNOWN_CYCLEWAY_VALUES

        private val HANDLED_CYCLEWAY_LANE_VALUES = KNOWN_CYCLEWAY_LANE_VALUES
    }
}

