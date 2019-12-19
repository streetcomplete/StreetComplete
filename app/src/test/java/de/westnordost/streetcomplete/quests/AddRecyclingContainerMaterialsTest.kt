package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import org.junit.Assert.*
import org.junit.Test

class AddRecyclingContainerMaterialsTest {
    private val questType = AddRecyclingContainerMaterials(mock())

    @Test fun `is not applicable to because it is not a container`() {
        val element = OsmNode(1,1,0.0,0.0, mapOf(
            "amenity" to "recycling",
            "recycling_type" to "centre"
        ))
        assertFalse(questType.isApplicableTo(element))
    }

    @Test fun `is not applicable because a recycling material is already set`() {
        val element = OsmNode(1,1,0.0,0.0, mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:your_moma_jokes" to "won't fit"
        ))
        assertFalse(questType.isApplicableTo(element))
    }

    @Test fun `is applicable`() {
        val element = OsmNode(1,1,0.0,0.0, mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container"
        ))
        assertTrue(questType.isApplicableTo(element))
    }

    @Test fun `apply normal answer`() {
        questType.verifyAnswer(
            listOf("recycling:a", "recycling:b"),
            StringMapEntryAdd("recycling:a", "yes"),
            StringMapEntryAdd("recycling:b", "yes")
        )
    }

    @Test fun `apply answer with plastic bottles`() {
        questType.verifyAnswer(
            listOf("recycling:plastic_bottles"),
            StringMapEntryAdd("recycling:plastic_bottles", "yes"),
            StringMapEntryAdd("recycling:plastic_packaging", "no"),
            StringMapEntryAdd("recycling:plastic", "no")
        )
    }

    @Test fun `apply answer with plastic packaging`() {
        questType.verifyAnswer(
            listOf("recycling:plastic_packaging"),
            StringMapEntryAdd("recycling:plastic_packaging", "yes"),
            StringMapEntryAdd("recycling:plastic", "no")
        )
    }
}
