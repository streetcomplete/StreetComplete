package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import de.westnordost.streetcomplete.quests.bikeway.AddCyclewayForm
import de.westnordost.streetcomplete.quests.bikeway.Cycleway

import org.mockito.Mockito.mock

class AddCyclewayTest : AOsmElementQuestTypeTest() {

    override val questType = AddCycleway(mock(OverpassMapDataDao::class.java))

    fun testCyclewayLeftAndRightDontHaveToBeSpecified1() {
        bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.EXCLUSIVE_LANE.name)
        val cb = StringMapChangesBuilder(tags)
        questType.applyAnswerTo(bundle, cb)
        // success if no exception thrown
    }

    fun testCyclewayLeftAndRightDontHaveToBeSpecified2() {
        bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.EXCLUSIVE_LANE.name)
        val cb = StringMapChangesBuilder(tags)
        questType.applyAnswerTo(bundle, cb)
        // success if no exception thrown
    }

    fun testCyclewayLane() {
        putBothSides(Cycleway.EXCLUSIVE_LANE)
        verify(
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    fun testCyclewayAdvisoryLane() {
        putBothSides(Cycleway.ADVISORY_LANE)
        verify(
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "advisory")
        )
    }

    fun testCyclewayUnspecifiedLane() {
        putBothSides(Cycleway.LANE_UNSPECIFIED)
        verify(StringMapEntryAdd("cycleway:both", "lane"))
    }

    fun testCyclewayTrack() {
        putBothSides(Cycleway.TRACK)
        verify(StringMapEntryAdd("cycleway:both", "track"))
    }

    fun testCyclewayBusLane() {
        putBothSides(Cycleway.BUSWAY)
        verify(StringMapEntryAdd("cycleway:both", "share_busway"))
    }

    fun testCyclewayPictogramLane() {
        putBothSides(Cycleway.PICTOGRAMS)
        verify(
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "pictogram")
        )
    }

    fun testCyclewaySuggestionLane() {
        putBothSides(Cycleway.SUGGESTION_LANE)
        verify(
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "advisory")
        )
    }

    fun testCyclewayNone() {
        putBothSides(Cycleway.NONE)
        verify(StringMapEntryAdd("cycleway:both", "no"))
    }

    fun testCyclewayOnSidewalk() {
        putBothSides(Cycleway.SIDEWALK_EXPLICIT)
        verify(
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("cycleway:both:segregated", "no")
        )
    }

    fun testCyclewaySidewalkOkay() {
        putBothSides(Cycleway.SIDEWALK_OK)
        verify(
            StringMapEntryAdd("cycleway:both", "no"),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("sidewalk:both:bicycle", "yes")
        )
    }

    fun testCyclewaySidewalkAny() {
        bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.SIDEWALK_EXPLICIT.name)
        bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.SIDEWALK_OK.name)
        verify(
            StringMapEntryAdd("sidewalk", "both")
        )
    }

    fun testCyclewayDualTrack() {
        putBothSides(Cycleway.DUAL_TRACK)
        verify(
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("cycleway:both:oneway", "no")
        )
    }

    fun testCyclewayDualLane() {
        putBothSides(Cycleway.DUAL_LANE)
        verify(
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:oneway", "no")
        )
    }

    fun testLeftAndRightAreDifferent() {
        bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.EXCLUSIVE_LANE.name)
        bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.TRACK.name)
        verify(
            StringMapEntryAdd("cycleway:right", "lane"),
            StringMapEntryAdd("cycleway:right:lane", "exclusive"),
            StringMapEntryAdd("cycleway:left", "track")
        )
    }

    fun testCyclewayMakesStreetNotOnewayForBicycles() {
        putBothSides(Cycleway.EXCLUSIVE_LANE)
        bundle.putBoolean(AddCyclewayForm.IS_ONEWAY_NOT_FOR_CYCLISTS, true)
        verify(
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("oneway:bicycle", "no"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    fun testCyclewayLaneWithExplicitDirection() {
        // this would be a street that has lanes on both sides but is oneway=yes (in countries with
        // right hand traffic)
        putBothSides(Cycleway.EXCLUSIVE_LANE)
        bundle.putInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR, -1)
        verify(
            StringMapEntryAdd("cycleway:left", "lane"),
            StringMapEntryAdd("cycleway:left:oneway", "-1"),
            StringMapEntryAdd("cycleway:right", "lane"),
            StringMapEntryAdd("cycleway:left:lane", "exclusive"),
            StringMapEntryAdd("cycleway:right:lane", "exclusive")
        )
    }

    fun testCyclewayLaneWithExplicitOtherDirection() {
        // this would be a street that has lanes on both sides but is oneway=-1 (in countries with
        // right hand traffic)
        putBothSides(Cycleway.EXCLUSIVE_LANE)
        bundle.putInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR, +1)
        verify(
            StringMapEntryAdd("cycleway:left", "lane"),
            StringMapEntryAdd("cycleway:left:oneway", "yes"),
            StringMapEntryAdd("cycleway:right", "lane"),
            StringMapEntryAdd("cycleway:left:lane", "exclusive"),
            StringMapEntryAdd("cycleway:right:lane", "exclusive")
        )
    }

    private fun putBothSides(cycleway: Cycleway) {
        bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, cycleway.name)
        bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, cycleway.name)
    }
}
