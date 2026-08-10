package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddClothingBinOperatorTest {

    private val questType = AddClothingBinOperator()

    @Test fun `is not applicable to empty tags`() {
        assertFalse(questType.isApplicableTo(node()))
    }

    @Test fun `is applicable to clothing container`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:paper" to "no",
        ))))
    }

    @Test fun `is not applicable to clothing container with operator`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "operator" to "DRK",
        ))))
    }

    @Test fun `is not applicable to clothing container with another recycling type`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:paper" to "yes",
        ))))
    }

    @Test fun `is applicable to clothing container with shoes recycling type`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "recycling",
            "recycling_type" to "container",
            "recycling:clothes" to "yes",
            "recycling:shoes" to "yes",
        ))))
    }
}
