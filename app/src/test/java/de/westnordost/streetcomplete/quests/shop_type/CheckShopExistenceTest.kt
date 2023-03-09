package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert
import org.junit.Test

class CheckShopExistenceTest {
    private val questType = CheckShopExistence(mock())

    @Test
    fun `applicable to old shops with unrecognised values and with name`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "weird_value", "name" to "Foobar")),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to old greengrocer shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }
}
