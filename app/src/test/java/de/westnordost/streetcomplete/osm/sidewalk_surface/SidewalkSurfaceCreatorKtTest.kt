package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SidewalkSurfaceCreatorKtTest {

    @Test fun `apply asphalt surface on both sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:both:surface", "asphalt")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(ASPHALT), SurfaceAndNote(ASPHALT)).appliedTo(
                mapOf()
            ),
        )
    }

    @Test fun `apply different surface on each side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:left:surface", "asphalt"),
                StringMapEntryAdd("sidewalk:right:surface", "paving_stones")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(ASPHALT), SurfaceAndNote(PAVING_STONES)).appliedTo(
                mapOf()
            ),
        )
    }

    @Test fun `apply generic surface on both sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:both:surface", "paved"),
                StringMapEntryAdd("sidewalk:both:surface:note", "note")
            ),
            LeftAndRightSidewalkSurface(
                SurfaceAndNote(PAVED, "note"),
                SurfaceAndNote(PAVED, "note")
            ).appliedTo(mapOf())
        )
    }

    @Test fun `updates check_date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk:both:surface", "asphalt", "asphalt"),
                StringMapEntryModify("check_date:sidewalk:surface", "2000-10-10", nowAsCheckDateString()),
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(ASPHALT), SurfaceAndNote(ASPHALT)).appliedTo(mapOf(
                "sidewalk:both:surface" to "asphalt",
                "check_date:sidewalk:surface" to "2000-10-10"
            ))
        )
    }

    @Test fun `sidewalk surface changes to be the same on both sides`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
                StringMapEntryDelete("sidewalk:right:surface", "paving_stones"),
                StringMapEntryAdd("sidewalk:both:surface", "concrete")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(CONCRETE), SurfaceAndNote(CONCRETE)).appliedTo(mapOf(
                "sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "paving_stones"
            ))
        )
    }

    @Test fun `sidewalk surface changes on each side`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk:left:surface", "asphalt", "concrete"),
                StringMapEntryModify("sidewalk:right:surface", "paving_stones", "gravel"),
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(CONCRETE), SurfaceAndNote(GRAVEL)).appliedTo(mapOf(
                "sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "paving_stones"
            ))
        )
    }

    @Test fun `smoothness tag removed when surface changes, same on both sides`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:both:smoothness", "excellent"),
                StringMapEntryModify("sidewalk:both:surface", "asphalt", "paving_stones")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(PAVING_STONES), SurfaceAndNote(PAVING_STONES)).appliedTo(mapOf(
                "sidewalk:both:surface" to "asphalt",
                "sidewalk:both:smoothness" to "excellent"
            ))
        )
    }

    @Test fun `remove smoothness when surface changes, different on each side`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
                StringMapEntryDelete("sidewalk:right:surface", "concrete"),
                StringMapEntryDelete("sidewalk:left:smoothness", "excellent"),
                StringMapEntryDelete("sidewalk:right:smoothness", "good"),
                StringMapEntryAdd("sidewalk:both:surface", "paving_stones")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(PAVING_STONES), SurfaceAndNote(PAVING_STONES)).appliedTo(mapOf(
                "sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "concrete",
                "sidewalk:left:smoothness" to "excellent",
                "sidewalk:right:smoothness" to "good"
            ))
        )
    }

    @Test fun `carriageway properties not affected by sidewalk answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk:both:surface", "paving_stones")
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(PAVING_STONES), SurfaceAndNote(PAVING_STONES)).appliedTo(mapOf(
                "sidewalk" to "both",
                "surface" to "concrete",
                "smoothness" to "excellent",
            ))
        )
    }
}

private fun LeftAndRightSidewalkSurface.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
