package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.surface.Surface.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SurfaceCreatorKtTest {

    @Test fun `apply surface`() {
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            ASPHALT.appliedTo(mapOf()),
        )
    }

    @Test fun `apply surface with prefix`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway:surface", "asphalt")),
            ASPHALT.appliedTo(mapOf(), "footway"),
        )
    }

    @Test fun `apply non-changed surface updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            ),
            ASPHALT.appliedTo(mapOf("surface" to "asphalt"))
        )
    }

    @Test fun `apply non-changed surface with prefix updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("footway:surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:footway:surface", nowAsCheckDateString())
            ),
            ASPHALT.appliedTo(mapOf("footway:surface" to "asphalt"), "footway")
        )
    }

    @Test fun `remove tracktype when surface category changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("tracktype", "grade5"),
                StringMapEntryDelete("check_date:tracktype", "2011-11-11"),
            ),
            ASPHALT.appliedTo(mapOf(
                "surface" to "compacted",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ))
        )
    }

    @Test fun `don't remove tracktype when surface was added`() {
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            ASPHALT.appliedTo(mapOf("tracktype" to "grade5",))
        )
    }

    @Test fun `don't remove tracktype when surface category didn't change`() {
        assertEquals(
            setOf(StringMapEntryModify("surface", "concrete", "asphalt")),
            ASPHALT.appliedTo(mapOf(
                "surface" to "concrete",
                "tracktype" to "grade5",
            ))
        )
    }

    @Test fun `remove mismatching tracktype not done with prefix`() {
        assertEquals(
            setOf(StringMapEntryModify("footway:surface", "compacted", "asphalt")),
            ASPHALT.appliedTo(mapOf(
                "footway:surface" to "compacted",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ), "footway")
        )
    }

    @Test fun `remove associated tags when surface changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("surface:note", "hey"),
                StringMapEntryDelete("surface:colour", "pink"),
                StringMapEntryDelete("smoothness", "well"),
                StringMapEntryDelete("smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:smoothness", "2011-11-11"),
                StringMapEntryDelete("tracktype", "grade5"),
            ),
            ASPHALT.appliedTo(mapOf(
                "surface" to "compacted",
                "surface:grade" to "3",
                "surface:note" to "hey",
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
            ASPHALT.appliedTo(mapOf(
                "surface" to "compacted",
                "surface:grade" to "3",
                "surface:note" to "hey",
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
                StringMapEntryDelete("footway:surface:note", "hey"),
                StringMapEntryDelete("footway:surface:colour", "pink"),
                StringMapEntryDelete("footway:smoothness", "well"),
                StringMapEntryDelete("footway:smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:footway:smoothness", "2011-11-11"),
            ),
            ASPHALT.appliedTo(mapOf(
                "footway:surface" to "compacted",
                "footway:surface:grade" to "3",
                "footway:surface:note" to "hey",
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
            ASPHALT.appliedTo(mapOf(
                "surface" to "asphalt",
                "surface:grade" to "3",
                "surface:note" to "hey",
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
            ASPHALT.appliedTo(mapOf(
                "footway:surface" to "asphalt",
                "footway:surface:grade" to "3",
                "footway:surface:note" to "hey",
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
            ASPHALT.appliedTo(mapOf(
                "highway" to "residential",
                "source:surface" to "bing"
            )),
        )
    }

    @Test fun `sidewalk surface marked as tag on road is not touched`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("surface", "asphalt"),
            ),
            ASPHALT.appliedTo(mapOf(
                "highway" to "tertiary",
                "sidewalk:surface" to "paving_stones"
            ))
        )
    }
}

private fun Surface.appliedTo(
    tags: Map<String, String>,
    prefix: String? = null
): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, prefix)
    return cb.create().changes
}
