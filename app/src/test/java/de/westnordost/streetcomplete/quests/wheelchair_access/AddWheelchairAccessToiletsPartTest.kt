package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert
import org.junit.Test

class AddWheelchairAccessToiletsPartTest {
    private val questType = AddWheelchairAccessToiletsPart()

    @Test
    fun `not applicable if wheelchair not limited`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "amenity" to "restaurant"
            )),
            node(tags = mapOf(
                "amenity" to "restaurant",
                "wheelchair" to "yes"
            )),
            node(tags = mapOf(
                "amenity" to "restaurant",
                "wheelchair" to "no"
            )),
        ))
        Assert.assertTrue(questType.getApplicableElements(mapData).toList().isEmpty())
    }

    @Test
    fun `applicable to place with toilets`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "wheelchair" to "limited",
                "shop" to "convenience",
                "toilets" to "yes"
            ))
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to restaurant with unknown toilet situation`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "wheelchair" to "limited",
                "amenity" to "restaurant"
            ))
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to cafe with indoor seating and unknown toilet situation`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "wheelchair" to "limited",
                "amenity" to "cafe",
                "indoor_seating" to "yes"
            ))
        ))
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to restaurant with no toilets`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "wheelchair" to "limited",
                "amenity" to "restaurant",
                "toilets" to "no"
            ))
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to restaurant with invalid toilets`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "wheelchair" to "limited",
                "amenity" to "restaurant",
                "toilets" to "customers"
            ))
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
