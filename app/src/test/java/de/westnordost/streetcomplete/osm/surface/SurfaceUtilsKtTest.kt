package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import kotlin.test.*
import kotlin.test.Test

class SurfaceUtilsKtTest {

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
                StringMapEntryAdd("surface", "paved")
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
