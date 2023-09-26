package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckShopExistenceTest {
    private val questType = CheckShopExistence { tags ->
        if (tags["shop"] == "greengrocer") mock() else null
    }

    @Test
    fun `not applicable to old shops with unrecognised values and without name`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "weird_value")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to old shops with unrecognised values and with name`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "weird_value", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to old greengrocer shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }
}
