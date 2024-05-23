package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import kotlin.test.*
import kotlin.test.Test

class SurfaceUtilsKtTest {

    @Test fun `poor tracktype conflicts with paved surface`() {
        assertTrue(isSurfaceAndTracktypeConflicting("asphalt", "grade5"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("asphalt", "grade5"))
    }

    @Test fun `high quality tracktype conflicts with poor surface`() {
        assertTrue(isSurfaceAndTracktypeConflicting("earth", "grade1"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("earth", "grade1"))
    }

    @Test fun `high quality tracktype fits good surface`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", "grade1"))
        assertFalse(isSurfaceAndTracktypeCombinationSuspicious("paving_stones", "grade1"))
    }

    @Test fun `unknown tracktype does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", "lorem ipsum"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("paving_stones", "lorem ipsum"))
    }

    @Test fun `unknown surface does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeConflicting("zażółć", "grade1"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("zażółć", "grade1"))
    }

    @Test fun `lower tracktype on paved is suspicious but not always conflicting`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", "grade2"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("paving_stones", "grade2"))
    }

    @Test fun `sand surface is conflicting and suspicious on tracktype=grade2`() {
        assertTrue(isSurfaceAndTracktypeConflicting("sand", "grade2"))
        assertTrue(isSurfaceAndTracktypeCombinationSuspicious("sand", "grade2"))
    }

    @Test fun `missing tracktype is not conflicting`() {
        assertFalse(isSurfaceAndTracktypeConflicting("paving_stones", null))
        assertFalse(isSurfaceAndTracktypeCombinationSuspicious("paving_stones", null))
    }

    @Test fun `update foot and cycleway with identical surface`() {
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "asphalt",
                "cycleway:surface" to "asphalt"
            ))
        )
    }

    @Test fun `update foot and cycleway with common paved surface`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "paved"),
                StringMapEntryModify("surface:note", "asphalt but also paving stones", "asphalt but also paving stones"),
            ),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "asphalt",
                "cycleway:surface" to "paving_stones",
                "surface:note" to "asphalt but also paving stones"
            ))
        )
    }

    @Test fun `update foot and cycleway with common unpaved surface`() {
        assertEquals(
            setOf(StringMapEntryAdd("surface", "unpaved")),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "gravel",
                "cycleway:surface" to "sand",
            ))
        )
    }

    @Test fun `update foot and cycleway with no common surface`() {
        assertEquals(
            setOf(),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "asphalt",
                "cycleway:surface" to "sand",
            ))
        )
    }

    @Test fun `removes common surface if foot and cycleway have nothing in common`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("surface", "paved"),
                StringMapEntryDelete("surface:note", "actually the cycleway is sand"),
                StringMapEntryDelete("smoothness", "as smooth as a sandbox"),
            ),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "asphalt",
                "cycleway:surface" to "sand",
                "surface" to "paved",
                "surface:note" to "actually the cycleway is sand",
                "smoothness" to "as smooth as a sandbox",
            ))
        )
    }

    @Test fun `removes old tags associated with surface when common surface is changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "paved"),
                StringMapEntryDelete("smoothness", "excellent"),
            ),
            appliedCommonSurfaceFromFootAndCyclewaySurface(mapOf(
                "footway:surface" to "asphalt",
                "cycleway:surface" to "paving_stones",
                "surface" to "asphalt",
                "smoothness" to "excellent",
            ))
        )
    }
}

private fun appliedCommonSurfaceFromFootAndCyclewaySurface(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    updateCommonSurfaceFromFootAndCyclewaySurface(cb)
    return cb.create().changes
}
