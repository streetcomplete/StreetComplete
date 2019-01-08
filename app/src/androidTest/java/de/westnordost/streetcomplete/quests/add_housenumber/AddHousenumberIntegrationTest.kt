package de.westnordost.streetcomplete.quests.add_housenumber

import androidx.test.filters.LargeTest
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.OsmModule
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsNoQuest
import org.junit.Test

@LargeTest
class AddHousenumberIntegrationTest {

    @Test fun unspecified_building_type_is_excluded() {
        //test is using https://www.openstreetmap.org/way/139545168/history, version 3
        verifyYieldsNoQuest(
            BoundingBox(50.06655, 19.93690, 50.06681, 19.93740),
            "2014-03-01"
        )
    }

    @Test fun underground_building_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(52.5149999, 13.417933, 52.516198, 13.4183836),
            "2018-03-01"
        )
    }

    @Test fun building_type_that_likely_has_no_address_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5312195, 9.9804139, 53.531559, 9.9808994),
            "2018-03-01"
        )
    }

    /* --------------------------------- buildings with address --------------------------------- */

    @Test fun building_with_address_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(52.3812058, 13.0742659, 52.3815491, 13.0748789),
            "2018-03-01"
        )
    }

    @Test fun relation_building_with_address_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5465806, 9.934811, 53.5473838, 9.9359912),
            "2018-03-01"
        )
    }

    /* --------------------------- buildings with address node inside --------------------------- */

    @Test fun building_with_address_inside_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(59.9152277, 10.7040524, 59.9155073, 10.7045299),
            "2018-03-01"
        )
    }

    @Test fun building_with_address_inside_but_outside_boundingBox_is_excluded_nevertheless() {
        verifyYieldsNoQuest(
            BoundingBox(59.9154105, 10.7041866, 59.9154966, 10.7045299),
            "2018-03-01"
        )
    }


    @Test fun relation_building_with_address_inside_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(59.9125977, 10.7393879, 59.9133372, 10.740847),
            "2018-03-01"
        )
    }

    @Test fun relation_building_with_address_inside_but_outside_boundingBox_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(59.9125506, 10.7404044, 59.9126326, 10.7405975),
            "2018-03-01"
        )
    }

    /* ------------------------- buildings with address node on outline ------------------------- */

    @Test fun building_with_address_on_outline_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(52.380765, 13.0748677, 52.3809697, 13.075211),
            "2018-03-01"
        )
    }

    @Test fun building_with_address_on_outline_but_outside_boundingBox_is_excluded_nevertheless() {
        verifyYieldsNoQuest(
            BoundingBox(52.3807601, 13.0748811, 52.3808796, 13.0752191),
            "2018-03-01"
        )
    }

    @Test fun relation_building_with_address_on_outline_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5546534, 9.9783272, 53.5550996, 9.9797165),
            "2018-03-01"
        )
    }

    @Test fun relation_building_with_address_on_outline_but_outside_boundingBox_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5546869, 9.9788234, 53.5549896, 9.9790755),
            "2018-03-01"
        )
    }

    /* --------------------------- buildings within area with address --------------------------- */

    @Test fun building_within_area_with_address_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5738493, 9.9408299, 53.5741742, 9.9416882),
            "2018-03-01"
        )
    }

    @Test fun building_within_relation_area_with_address_is_excluded() {
        verifyYieldsNoQuest(
            BoundingBox(53.5054499, 10.1937836, 53.5061231, 10.1943254),
            "2018-03-01"
        )
    }

    private fun verifyYieldsNoQuest(bbox: BoundingBox, date: String) {
        val o = OsmModule.overpassOldMapDataDao({ OsmModule.overpassMapDataParser() }, date)
        val quest = AddHousenumber(o)
        quest.verifyDownloadYieldsNoQuest(bbox)
    }
}
