package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class SurfaceCreatorKtTest {

    @Test fun `apply surface`() {
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf("highway" to "residential")),
        )
    }

    @Test fun `apply non-changed surface updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "surface" to "asphalt"
            ))
        )
    }

    @Test fun `remove mismatching tracktype`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("tracktype", "grade5"),
                StringMapEntryDelete("check_date:tracktype", "2011-11-11"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ))
        )
    }

    @Test fun `keep matching tracktype`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt")
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "tracktype" to "grade1"
            ))
        )
    }

    @Test fun `remove associated tags when surface changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("surface:colour", "pink"),
                StringMapEntryDelete("smoothness", "well"),
                StringMapEntryDelete("smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:smoothness", "2011-11-11"),
                StringMapEntryDelete("tracktype", "grade5"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "surface" to "compacted",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade5",
                "surface:colour" to "pink"
            ))
        )
    }

    @Test fun `keep associated tags when surface did not change`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade1",
                "surface:colour" to "pink"
            ))
        )
    }

    @Test fun `always remove source-surface`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("source:surface", "bing"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "residential",
                "source:surface" to "bing"
            )),
        )
    }

    @Test fun `add note when specified`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryAdd("surface:note", "gurgle"),
            ),
            SurfaceAndNote(Surface.ASPHALT, "gurgle").appliedTo(mapOf()),
        )
    }

    @Test fun `remove note when not specified`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("surface:note", "nurgle"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf("surface:note" to "nurgle")),
        )
    }

    @Test fun `sidewalk surface marked as tag on road is not touched`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "highway" to "tertiary",
                "sidewalk:surface" to "paving_stones"
            ))
        )
    }
}

private fun SurfaceAndNote.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
