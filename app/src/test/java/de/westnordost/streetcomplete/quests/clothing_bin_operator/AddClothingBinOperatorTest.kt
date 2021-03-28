package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.osmapi.map.data.OsmNode
import org.junit.Assert.*
import org.junit.Test

class AddClothingBinOperatorTest {

    private val questType = AddClothingBinOperator()

    @Test fun `is not applicable to null tags`() {
        assertFalse(questType.isApplicableTo(create(null)))
    }

    @Test fun `is applicable to clothing container`() {
        assertTrue(questType.isApplicableTo(create(mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:paper" to "no",
        ))))
    }

    @Test fun `is not applicable to clothing container with operator`() {
        assertFalse(questType.isApplicableTo(create(mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "operator" to "DRK",
        ))))
    }

    @Test fun `is not applicable to clothing container with another recycling type`() {
        assertFalse(questType.isApplicableTo(create(mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:paper" to "yes",
        ))))
    }

    @Test fun `is applicable to clothing container with shoes recycling type`() {
        assertTrue(questType.isApplicableTo(create(mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:shoes" to "yes",
        ))))
    }

    private fun create(tags: Map<String, String>?) = OsmNode(1L, 1, 0.0, 0.0, tags)
}