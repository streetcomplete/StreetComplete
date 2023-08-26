package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Assert
import kotlin.test.Test

class AddBuildingEntranceTest {

    private val questType = AddEntrance()

    private fun generalTestDataWithWayThroughBuilding(tagsOnWayThroughBuilding: Map<String, String>): TestMapDataWithGeometry {
        return TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments",
                )),
                way(2L, listOf(1, 3), tagsOnWayThroughBuilding),
                way(3L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
    }

    @Test
    fun `applicable to cases where corridor through building is mapped`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "corridor",
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to cases where corridor through building is mapped and amrked as private`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "corridor",
            "access" to "private",
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to cases where indor footway through building is mapped`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "indoor" to "yes",
        ))
        // 2 because both ends of way through building generated quest
        Assert.assertEquals(2, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `footway without tags is treated as indoor`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
        ))
        // 2 because both ends of way through building generated quest
        Assert.assertEquals(2, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `footway through building may be marked as private`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "access" to "private",
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to building passage openings`() {
        // https://www.openstreetmap.org/node/8492154414#map=19/50.07511/20.05013
        // should not trigger entrance quest
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "tunnel" to "building_passage",
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to building passage openings tagged only with unusual covered values`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "covered" to "weird_value",
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to building passage openings tagged only with unusual tunnel values`() {
        val mapData = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "tunnel" to "weird_value",
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to plausible entrances`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments",
                )),
                way(2L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to underground buildings`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments",
                    "location" to "underground",
                )),
                way(2L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to likely underground buildings waiting for AddIsBuildingUnderground quest`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments",
                    "layer" to "-3",
                )),
                way(2L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to buildings with negative layer`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments",
                    "layer" to "-3",
                    "location" to "surface",
                )),
                way(2L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to footway entering rooftop which may be without an actual entrance`() {
        // see https://github.com/streetcomplete/StreetComplete/issues/4805
        val mapDataWithRoof = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "location" to "roof",
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapDataWithRoof).toList().size)

        val mapDataWithRooftop = generalTestDataWithWayThroughBuilding(mapOf(
            "highway" to "footway",
            "location" to "rooftop",
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapDataWithRooftop).toList().size)
    }

    @Test
    fun `applicable to multipolygon buildings`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
                way(2L, listOf(1, 2, 3)),
                way(3L, listOf(3, 4, 1)),
                rel(1L, listOf(member(ElementType.WAY, 2), member(ElementType.WAY, 3)), mapOf(
                    "building" to "apartments",
                    "type" to "multipolygon"
                )),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to non-multipolygon building relations`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                rel(1L, listOf(member(ElementType.WAY, 2), member(ElementType.WAY, 3)), mapOf(
                    "building" to "apartments",
                    "type" to "site"
                )),
                way(1L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
                way(2L, listOf(1, 2)),
                way(3L, listOf(3, 4)),
            ),
        )
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
