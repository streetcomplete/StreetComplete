package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.bikeway.*
import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*
import org.junit.Test

import org.mockito.Mockito.mock

class AddCyclewayTest {

    private val questType = AddCycleway(mock(OverpassMapDataDao::class.java))

    @Test fun `apply cycleway lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(EXCLUSIVE_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    @Test fun `apply advisory lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(ADVISORY_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "advisory")
        )
    }

    @Test fun `apply cycleway track answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(TRACK),
            StringMapEntryAdd("cycleway:both", "track")
        )
    }

    @Test fun `apply unspecified cycle lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(LANE_UNSPECIFIED),
            StringMapEntryAdd("cycleway:both", "lane")
        )
    }

    @Test fun `apply bus lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(BUSWAY),
            StringMapEntryAdd("cycleway:both", "share_busway")
        )
    }

    @Test fun `apply pictogram lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(PICTOGRAMS),
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "pictogram")
        )
    }

    @Test fun `apply suggestion lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(PICTOGRAMS),
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "pictogram")
        )
    }

    @Test fun `apply no cycleway answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(NONE),
            StringMapEntryAdd("cycleway:both", "no")
        )
    }

    @Test fun `apply cycleway on sidewalk answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(SIDEWALK_EXPLICIT),
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("cycleway:both:segregated", "no")
        )
    }

    @Test fun `apply cycleway on sidewalk permitted answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(SIDEWALK_OK),
            StringMapEntryAdd("cycleway:both", "no"),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("sidewalk:both:bicycle", "yes")
        )
    }

    private fun bothSidesAnswer(bothSides: Cycleway): CyclewayAnswer {
        val side = CyclewaySide(bothSides)
        return CyclewayAnswer(side, side)
    }


    @Test fun `apply answer where both sides are different but both tag sidewalk`() {
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(SIDEWALK_OK), CyclewaySide(SIDEWALK_EXPLICIT)),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("cycleway:left", "no"),
            StringMapEntryAdd("sidewalk:left:bicycle", "yes"),
            StringMapEntryAdd("cycleway:right", "track"),
            StringMapEntryAdd("cycleway:right:segregated", "no")
        )
    }

    @Test fun `apply dual cycle track answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(DUAL_TRACK),
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("cycleway:both:oneway", "no")
        )
    }

    @Test fun `apply dual cycle lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(DUAL_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:oneway", "no"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    @Test fun `apply answer where left and right side are different`() {
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK), CyclewaySide(NONE)),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:right", "no")
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of oneway`() {
        // this would be a street that has tracks on both sides but is oneway=yes (in countries with
        // right hand traffic)
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK, -1), CyclewaySide(TRACK), true),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:left:oneway", "-1"),
            StringMapEntryAdd("cycleway:right", "track"),
            StringMapEntryAdd("oneway:bicycle", "no")
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of backward oneway`() {
        // this would be a street that has lanes on both sides but is oneway=-1 (in countries with
        // right hand traffic)
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK, +1), CyclewaySide(TRACK), true),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:left:oneway", "yes"),
            StringMapEntryAdd("cycleway:right", "track"),
            StringMapEntryAdd("oneway:bicycle", "no")
        )
    }
}
