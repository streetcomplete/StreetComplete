package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceKtTest {
    @Test
    fun `poor tracktype conflicts with paved surface`() {
        assertTrue(isSurfaceAndTracktypeMismatching("asphalt", "grade5"))
    }

    @Test
    fun `high quality tracktype conflicts with poor surface`() {
        assertTrue(isSurfaceAndTracktypeMismatching("gravel", "grade1"))
    }

    @Test
    fun `high quality tracktype fits good surface`() {
        assertFalse(isSurfaceAndTracktypeMismatching("paving_stones", "grade1"))
    }

    @Test
    fun `unknown tracktype does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeMismatching("paving_stones", "lorem ipsum"))
    }

    @Test
    fun `unknown surface does not crash or conflict`() {
        assertFalse(isSurfaceAndTracktypeMismatching("zażółć", "grade1"))
    }

    @Test
    fun `specific surface generates specific surface status for roads`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt")).value, Surface.ASPHALT)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt")).note, null)
    }

    @Test
    fun `specific surface generates specific surface status for paths`() {
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).main.value, Surface.ASPHALT)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).main.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt")).footway.note, null)
    }

    @Test
    fun `note tag results in a different status for roads`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).value, Surface.ASPHALT)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).note, "useful info")
    }

    @Test
    fun `note tag results in a different status for paths`() {
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).main.value, Surface.ASPHALT)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).main.note, "useful info")
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "asphalt", "surface:note" to "useful info")).footway.note, null)
    }

    @Test
    fun `paved and unpaved is treated as missing surface for roads and paths`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "unpaved")).value, null)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "unpaved")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).main.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).main.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved")).footway.note, null)
    }

    @Test
    fun `paved and unpaved is not removed when with note for both roads and paths`() {
        assertTrue(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).main.value in listOf(Surface.UNPAVED_ROAD, Surface.UNPAVED_AREA))
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).main.note, "foobar")
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "unpaved", "surface:note" to "foobar")).footway.note, null)
    }

    @Test
    fun `cobblestone is treated as missing surface for roads and paths`() {
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "cobblestone")).value, null)
        assertEquals(createMainSurfaceStatus(mapOf("surface" to "cobblestone")).note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).main.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).main.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone")).footway.note, null)
    }

    @Test
    fun `cobblestone is treated as missing surface for roads and paths also when with note but note is shown`() {
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).main.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).main.note, "foobar")
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).cycleway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).cycleway.note, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).footway.value, null)
        assertEquals(createSurfaceStatus(mapOf("surface" to "cobblestone", "surface:note" to "foobar")).footway.note, null)
    }

    @Test
    fun `surface note is taken into account when generating surface status for path with cycleway and footway split`() {
        // https://www.openstreetmap.org/way/925626513 version 4
        val tags = mapOf(
            "bicycle" to "designated",
            "cycleway:surface" to "paving_stones",
            "foot" to "designated",
            "footway:surface" to "asphalt",
            "highway" to "path",
            "lit" to "yes",
            "oneway:bicycle" to "yes",
            "path" to "sidewalk",
            "segregated" to "yes",
            "surface" to "paving_stones",
            "surface:note" to "Rad Pflastersteine Fußgänger Asphalt",
        )
        val status = createSurfaceStatus(tags)
        assertEquals("Rad Pflastersteine Fußgänger Asphalt", status.main.note)
    }

    @Test
    fun `find shared surface for two paved`() {
        assertEquals("paved", commonSurfaceDescription("asphalt", "paving_stones"))
        assertTrue(commonSurfaceObject("asphalt", "paving_stones")!!.osmValue == "paved")
    }

    @Test
    fun `find shared surface for two unpaved`() {
        assertEquals("unpaved", commonSurfaceDescription("gravel", "sand"))
        assertTrue(commonSurfaceObject("gravel", "sand")!!.osmValue == "unpaved")
    }

    @Test
    fun `find shared surface for two identical`() {
        assertEquals("sand", commonSurfaceDescription("sand", "sand"))
        assertEquals(Surface.SAND, commonSurfaceObject("sand", "sand"))
    }

    @Test
    fun `find shared surface for two without shared surface`() {
        assertEquals(null, commonSurfaceDescription("asphalt", "sand"))
        assertEquals(null, commonSurfaceObject("asphalt", "sand"))
    }

    @Test
    fun `converting tags to enum works`() {
        assertEquals(Surface.ASPHALT, surfaceTextValueToSurfaceEnum("asphalt"))
        assertEquals(Surface.SAND, surfaceTextValueToSurfaceEnum("sand"))
    }

    @Test
    fun `converting tags to enum supports synonyms`() {
        assertEquals(Surface.EARTH, surfaceTextValueToSurfaceEnum("earth"))
        assertEquals(Surface.SOIL, surfaceTextValueToSurfaceEnum("soil"))
    }

    @Test
    fun `converting tags to enum return null for ones that should be retagged`() {
        assertEquals(null, surfaceTextValueToSurfaceEnum("cement"))
        assertEquals(null, surfaceTextValueToSurfaceEnum("cobblestone"))
    }

    @Test
    fun `converting tags to enum can produce UNKNOWN_SURFACE`() {
        assertEquals(Surface.UNKNOWN_SURFACE, surfaceTextValueToSurfaceEnum("weird_specific_value"))
    }

    @Test
    fun `converting tags to enum treats outcomes of bad merges as invalid`() {
        assertEquals(null, surfaceTextValueToSurfaceEnum("paved;unpaved"))
        assertEquals(null, surfaceTextValueToSurfaceEnum("<different>"))
    }

    @Test
    fun `check date is among keys removed on surface change`() {
        assertTrue("check_date:cycleway:surface" in keysToBeRemovedOnSurfaceChange("cycleway:"))
    }

    @Test
    fun `surface=unpaved is underspecified and must be described`() {
        assertTrue(Surface.UNPAVED_ROAD.shouldBeDescribed)
        assertTrue(Surface.UNPAVED_AREA.shouldBeDescribed)
    }

    @Test
    fun `surface=asphalt is well specified and does not need description`() {
        assertFalse(Surface.ASPHALT.shouldBeDescribed)
    }

    @Test
    fun `surface=ground is underspecified and does not need description`() {
        assertFalse(Surface.GROUND_AREA.shouldBeDescribed)
        assertFalse(Surface.GROUND_ROAD.shouldBeDescribed)
    }

    @Test fun `apply surface`() {
        verifyApplyTo(
            mapOf("highway" to "residential"),
            Surface.ASPHALT,
            arrayOf(StringMapEntryAdd("surface", "asphalt"))
        )
    }

    @Test fun `apply non-changed surface`() {
        verifyApplyTo(
            mapOf(
                "highway" to "residential",
                "surface" to "asphalt"
            ),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            )
        )
    }

    @Test fun `remove mismatching tracktype`() {
        verifyApplyTo(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("tracktype", "grade5"),
                StringMapEntryDelete("check_date:tracktype", "2011-11-11"),
            )
        )
    }

    @Test fun `keep matching tracktype`() {
        verifyApplyTo(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade1"
            ),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt")
            )
        )
    }

    @Test fun `removes associated values when surface changed`() {
        verifyApplyTo(
            mapOf(
                "highway" to "residential",
                "surface" to "compacted",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade5"
            ),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("smoothness", "well"),
                StringMapEntryDelete("smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:smoothness", "2011-11-11"),
                StringMapEntryDelete("tracktype", "grade5"),
            )
        )
    }

    @Test fun `always removes source-surface`() {
        verifyApplyTo(
            mapOf("highway" to "residential", "source:surface" to "bing"),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("source:surface", "bing"),
            )
        )
    }

    @Test fun `add note when specified`() {
        verifyApplyTo(
            mapOf(),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryAdd("surface:note", "gurgle"),
            ),
            note = "gurgle",
        )
    }

    @Test fun `remove note when not specified`() {
        verifyApplyTo(
            mapOf("surface:note" to "nurgle"),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("surface:note", "nurgle"),
            )
        )
    }

    @Test fun `remove surface colour when changing surface`() {
        verifyApplyTo(
            mapOf("surface:colour" to "transparent", "surface" to "mud"),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryModify("surface", "mud", "asphalt"),
                StringMapEntryDelete("surface:colour", "transparent"),
            )
        )
    }

    @Test fun `keep surface colour when not changing surface`() {
        verifyApplyTo(
            mapOf("surface:colour" to "transparent", "surface" to "asphalt"),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `sidewalk surface marked as tag on road is not touched`() {
        verifyApplyTo(
            mapOf("highway" to "tertiary", "sidewalk:surface" to "paving_stones"),
            Surface.ASPHALT,
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
            )
        )
    }
}

private fun verifyApplyTo(tags: Map<String, String>, answer: Surface, expectedChanges: Array<StringMapEntryChange>, note: String? = null) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb, note = note)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
