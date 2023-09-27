package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.*
import kotlin.test.Test

class SurfaceUtilsKtTest {

    @Test fun `poor tracktype conflicts with paved surface`() {
        assertTrue(isSurfaceAndTracktypeConflicting("asphalt", "grade5"))
    }

    @Test fun `high quality tracktype conflicts with poor surface`() {
        assertTrue(isSurfaceAndTracktypeConflicting("gravel", "grade1"))
    }

    @Test fun `high quality tracktype fits good surface`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", "grade1"))
    }

    @Test fun `unknown tracktype does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", "lorem ipsum"))
    }

    @Test fun `unknown surface does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeConflicting("zażółć", "grade1"))
    }

    @Test fun `update foot and cycleway with identical surface`() {
        val tags = StringMapChangesBuilder(mapOf(
            "footway:surface" to "asphalt",
            "cycleway:surface" to "asphalt"
        ))
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        tags.create().changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryAdd("surface", "asphalt")
        ))
    }

    @Test fun `update foot and cycleway with common paved surface`() {
        val tags = StringMapChangesBuilder(mapOf(
            "footway:surface" to "asphalt",
            "cycleway:surface" to "paving_stones",
            "surface:note" to "asphalt but also paving stones"
        ))
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        tags.create().changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryAdd("surface", "paved")
        ))
    }

    @Test fun `update foot and cycleway with common unpaved surface`() {
        val tags = StringMapChangesBuilder(mapOf(
            "footway:surface" to "gravel",
            "cycleway:surface" to "sand",
        ))
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        tags.create().changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryAdd("surface", "unpaved")
        ))
    }

    @Test fun `update foot and cycleway with no common surface`() {
        val tags = StringMapChangesBuilder(mapOf(
            "footway:surface" to "asphalt",
            "cycleway:surface" to "sand",
        ))
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        tags.create().changes.isEmpty()
    }

    @Test fun `removes common surface if foot and cycleway have nothing in common`() {
        val tags = StringMapChangesBuilder(mapOf(
            "footway:surface" to "asphalt",
            "cycleway:surface" to "sand",
            "surface" to "paved",
            "surface:note" to "actually the cycleway is sand"
        ))
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        tags.create().changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryDelete("surface", "paved"),
            StringMapEntryDelete("surface:note", "actually the cycleway is sand")
        ))
    }
}
