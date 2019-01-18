package de.westnordost.streetcomplete.quests.construction

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.IntegrationTests
import de.westnordost.streetcomplete.data.OsmModule
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsNoQuest
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsQuest
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(IntegrationTests::class)
class MarkCompletedBuildingConstructionIntegrationTest {

    @Test fun `matching candidate is accepted`() {
        //https://www.openstreetmap.org/way/494183785#map=19/50.07671/19.94703
        verifyYieldsQuest(
            BoundingBox(50.07664, 19.94671, 50.07672, 19.94700),
            "2018-03-01"
        )
    }

    @Test fun `fresh construction is not accepted`() {
        //https://www.openstreetmap.org/way/494183785#map=19/50.07671/19.94703
        verifyYieldsNoQuest(
            BoundingBox(50.07664, 19.94671, 50.07672, 19.94700),
            "2017-07-30"
        )
    }

    @Test fun `relations are accepted`() {
        //https://www.openstreetmap.org/relation/7405013
        verifyYieldsQuest(
            BoundingBox(55.89375, 37.53794, 55.89441, 37.53857),
            "2018-03-01"
        )
    }

    private fun verifyYieldsNoQuest(bbox: BoundingBox, date: String) {
        val o = OsmModule.overpassOldMapDataDao({ OsmModule.overpassMapDataParser() }, date)
        val quest = MarkCompletedBuildingConstructionOldData(o, date)
        quest.verifyDownloadYieldsNoQuest(bbox)
    }

    private fun verifyYieldsQuest(bbox: BoundingBox, date: String) {
        val o = OsmModule.overpassOldMapDataDao({ OsmModule.overpassMapDataParser() }, date)
        val quest = MarkCompletedBuildingConstructionOldData(o, date)
        quest.verifyDownloadYieldsQuest(bbox)
    }
}
