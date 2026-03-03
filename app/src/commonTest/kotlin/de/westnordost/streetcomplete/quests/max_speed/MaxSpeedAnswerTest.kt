package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit.*
import de.westnordost.streetcomplete.quests.max_speed.MaxSpeedSign.Type.*
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import kotlin.test.Test
import kotlin.test.assertEquals

class MaxSpeedAnswerTest {
    @Test fun `apply no sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "XX:urban")),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.URBAN).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "XX:rural")),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.RURAL).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "XX:motorway")),
            MaxSpeedAnswer.NoSign.appliedTo(mapOf("highway" to "motorway"))
        )
    }

    @Test fun `apply sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            MaxSpeedSign(Speed(123, KILOMETERS_PER_HOUR), NORMAL).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123 mph"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            MaxSpeedSign(Speed(123, MILES_PER_HOUR), NORMAL).appliedTo(mapOf())
        )
    }

    @Test fun `apply advisory sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:advisory", "123"),
                StringMapEntryAdd("maxspeed:type:advisory", "sign")
            ),
            MaxSpeedSign(Speed(123, KILOMETERS_PER_HOUR), ADVISORY).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:advisory", "123 mph"),
                StringMapEntryAdd("maxspeed:type:advisory", "sign")
            ),
            MaxSpeedSign(Speed(123, MILES_PER_HOUR), ADVISORY).appliedTo(mapOf())
        )
    }

    @Test fun `apply zone sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "XX:zone123")
            ),
            MaxSpeedSign(Speed(123, KILOMETERS_PER_HOUR), ZONE).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "15 mph"),
                StringMapEntryAdd("maxspeed:type", "ZZ:zone15")
            ),
            MaxSpeedSign(Speed(15, MILES_PER_HOUR), ZONE).appliedTo(mapOf())
        )
    }

    @Test fun `apply living street answer`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "residential", "living_street")),
            MaxSpeedAnswer.IsLivingStreet.appliedTo(mapOf("highway" to "residential"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("living_street", "yes")),
            MaxSpeedAnswer.IsLivingStreet.appliedTo(mapOf("highway" to "service"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("living_street", "yes")),
            MaxSpeedAnswer.IsLivingStreet.appliedTo(mapOf("highway" to "footway"))
        )
    }

    @Test fun `apply nsl answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "XX:nsl_restricted"),
                StringMapEntryAdd("lit", "yes"),
            ),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.RESTRICTED).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "XX:nsl_single"),
                StringMapEntryAdd("lit", "no"),
            ),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.SINGLE).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "XX:nsl_dual"),
                StringMapEntryAdd("lit", "no"),
                StringMapEntryAdd("dual_carriageway", "yes"),
            ),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.DUAL).appliedTo(mapOf())
        )
    }

    @Test fun `use subdivision (only) in regions where it matters`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "BE-VLG:urban")),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.URBAN).appliedTo(mapOf(), "BE-VLG")
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "DE:urban")),
            MaxSpeedAnswer.NoSignWithRoadType(RoadType.URBAN).appliedTo(mapOf(), "DE-HH")
        )
    }
}

private fun MaxSpeedAnswer.appliedTo(
    tags: Map<String, String>,
    countryCode: String = "XX"
): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, countryCode)
    return cb.create().changes
}
