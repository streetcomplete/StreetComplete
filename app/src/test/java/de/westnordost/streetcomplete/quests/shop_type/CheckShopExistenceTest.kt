package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckShopExistenceTest {
    // dummy
    @Mock private lateinit var feature: Feature

    private val questType = CheckShopExistence { element ->
        if (element.tags["shop"] == "greengrocer") mock(classOf<Feature>()) else null
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
