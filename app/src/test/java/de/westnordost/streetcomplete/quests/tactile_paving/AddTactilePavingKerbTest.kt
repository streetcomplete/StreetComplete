package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddTactilePavingKerbTest {
    private val questType = AddTactilePavingKerb()

    @Test fun `applicable to kerb with height`() {
        val node = node(id = 1, tags = mapOf("barrier" to "kerb", "kerb" to "lowered"))
        val mapData = TestMapDataWithGeometry(listOf(node, way(nodes = listOf(1, 2, 3), tags = mapOf(
            "highway" to "footway",
            "footway" to "sidewalk"
        ))))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(node))
    }
}
