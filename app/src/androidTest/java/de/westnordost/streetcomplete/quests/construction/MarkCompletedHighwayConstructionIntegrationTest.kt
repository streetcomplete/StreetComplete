package de.westnordost.streetcomplete.quests.construction

import androidx.test.filters.LargeTest
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.OsmModule
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsNoQuest
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsQuest
import org.junit.Test

@LargeTest
class MarkCompletedHighwayConstructionIntegrationTest {

    @Test fun old_highway_construction_triggers_quest() {
        //https://www.openstreetmap.org/way/298656945 edited on 2014-08-18
        verifyYieldsQuest(
            BoundingBox(40.01422, -3.02250, 40.01694, -3.02134),
            "2018-03-10"
        )
    }

    @Test fun new_highway_construction_is_not_triggering_quest() {
        //https://www.openstreetmap.org/way/298656945 edited on 2014-08-18
        verifyYieldsNoQuest(
            BoundingBox(40.01422, -3.02250, 40.01694, -3.02134),
            "2014-08-20"
        )
    }

    @Test fun opening_date_tag_used_to_filter_out_active_construction() {
        //https://www.openstreetmap.org/way/22462987 - 2017-06-30 not generating, 2017-07-01 generating quest
        verifyYieldsNoQuest(
            BoundingBox(47.80952, 12.09730, 47.81005, 12.09801),
            "2017-06-30"
        )
    }

    @Test fun opening_date_tag_ignored_if_outdated() {
        //https://www.openstreetmap.org/way/22462987 - 2017-06-30 not generating, 2017-07-01 generating quest
        verifyYieldsQuest(
            BoundingBox(47.80952, 12.09730, 47.81005, 12.09801),
            "2017-07-01"
        )
    }


    private fun verifyYieldsNoQuest(bbox: BoundingBox, date: String) {
        val o = OsmModule.overpassOldMapDataDao({ OsmModule.overpassMapDataParser() }, date)
        val quest = MarkCompletedHighwayConstructionOldData(o, date)
        quest.verifyDownloadYieldsNoQuest(bbox)
    }

    private fun verifyYieldsQuest(bbox: BoundingBox, date: String) {
        val o = OsmModule.overpassOldMapDataDao({ OsmModule.overpassMapDataParser() }, date)
        val quest = MarkCompletedHighwayConstructionOldData(o, date)
        quest.verifyDownloadYieldsQuest(bbox)
    }
}
