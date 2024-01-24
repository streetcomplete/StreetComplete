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
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf()),
        )
    }

    @Test fun `apply surface with prefix`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway:surface", "asphalt")),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(), "footway"),
        )
    }

    @Test fun `apply non-changed surface updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf("surface" to "asphalt"))
        )
    }

    @Test fun `apply non-changed surface with prefix updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("footway:surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:footway:surface", nowAsCheckDateString())
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf("footway:surface" to "asphalt"), "footway")
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
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ))
        )
    }

    @Test fun `remove mismatching tracktype not done with prefix`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway:surface", "asphalt")),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ), "footway")
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

    @Test fun `do not remove associated tags of main surface when surface with prefix changed`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("footway:surface", "asphalt")
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "surface" to "compacted",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade5",
                "surface:colour" to "pink"
            ), "footway")
        )
    }

    @Test fun `remove associated tags when surface with prefix changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("footway:surface", "compacted", "asphalt"),
                StringMapEntryDelete("footway:surface:grade", "3"),
                StringMapEntryDelete("footway:surface:colour", "pink"),
                StringMapEntryDelete("footway:smoothness", "well"),
                StringMapEntryDelete("footway:smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:footway:smoothness", "2011-11-11"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "footway:surface" to "compacted",
                "footway:surface:grade" to "3",
                "footway:smoothness" to "well",
                "footway:smoothness:date" to "2011-11-11",
                "check_date:footway:smoothness" to "2011-11-11",
                "footway:surface:colour" to "pink",
            ), "footway")
        )
    }

    @Test fun `keep associated tags when surface did not change`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
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

    @Test fun `keep associated tags when surface with prefix did not change`() {
        assertEquals(
            setOf(
                StringMapEntryModify("footway:surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:footway:surface", nowAsCheckDateString()),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(mapOf(
                "footway:surface" to "asphalt",
                "footway:surface:grade" to "3",
                "footway:smoothness" to "well",
                "footway:smoothness:date" to "2011-11-11",
                "check_date:footway:smoothness" to "2011-11-11",
                "footway:surface:colour" to "pink"
            ), "footway")
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

    @Test fun `add note with prefix when specified`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("footway:surface", "asphalt"),
                StringMapEntryAdd("footway:surface:note", "gurgle"),
            ),
            SurfaceAndNote(Surface.ASPHALT, "gurgle").appliedTo(mapOf(), "footway"),
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

    @Test fun `remove note with prefix when not specified`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("footway:surface", "asphalt"),
                StringMapEntryDelete("footway:surface:note", "nurgle"),
            ),
            SurfaceAndNote(Surface.ASPHALT).appliedTo(
                mapOf("footway:surface:note" to "nurgle"),
                "footway"
            ),
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

private fun SurfaceAndNote.appliedTo(
    tags: Map<String, String>,
    prefix: String? = null
): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, prefix)
    return cb.create().changes
}
