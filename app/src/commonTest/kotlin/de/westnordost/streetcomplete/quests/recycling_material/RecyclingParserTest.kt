package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RecyclingParserTest {

    @Test
    fun parse_empty_tags() {
        assertEquals(emptySet(), parseRecyclingMaterials(emptyMap()))
    }

    @Test
    fun parse_single_materials() {
        val tags = mapOf(
            "recycling:paper" to "yes",
            "recycling:glass_bottles" to "yes",
        )
        assertEquals(
            setOf(PAPER, GLASS_BOTTLES),
            parseRecyclingMaterials(tags),
        )
    }

    @Test
    fun parse_ignores_non_yes_tags() {
        val tags = mapOf(
            "recycling:paper" to "yes",
            "recycling:glass_bottles" to "no",
            "recycling:batteries" to "limited",
        )
        assertEquals(
            setOf(PAPER),
            parseRecyclingMaterials(tags),
        )
    }

    @Test
    fun parse_removes_child_when_parent_present() {
        val tags = mapOf(
            "recycling:glass" to "yes",
            "recycling:glass_bottles" to "yes",
        )
        assertEquals(
            setOf(GLASS),
            parseRecyclingMaterials(tags),
        )
    }

    @Test
    fun parse_removes_grandchild_when_parent_present() {
        val tags = mapOf(
            "recycling:plastic_packaging" to "yes",
            "recycling:pet_drink_bottles" to "yes",
        )
        assertEquals(
            setOf(PLASTIC_PACKAGING),
            parseRecyclingMaterials(tags),
        )
    }

    @Test
    fun parse_does_not_remove_child_when_parent_not_present()
    {
        val tags = mapOf(
            "recycling:pet_drink_bottles" to "yes",
        )
        assertEquals(
            setOf(PET_BOTTLES),
            parseRecyclingMaterials(tags),
        )
    }
}
