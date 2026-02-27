package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit.*
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
            DefaultMaxSpeed("XX", RoadType.RuralOrUrban.URBAN).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "YY:rural")),
            DefaultMaxSpeed("YY", RoadType.RuralOrUrban.RURAL).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "ZZ:motorway")),
            DefaultMaxSpeed("ZZ", null).appliedTo(mapOf(
                "highway" to "motorway"
            ))
        )
    }

    @Test fun `apply sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            MaxSpeedSign(Speed(123, KILOMETERS_PER_HOUR)).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123 mph"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            MaxSpeedSign(Speed(123, MILES_PER_HOUR)).appliedTo(mapOf())
        )
    }

    @Test fun `apply advisory sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:advisory", "123"),
                StringMapEntryAdd("maxspeed:type:advisory", "sign")
            ),
            AdvisorySpeedSign(Speed(123, KILOMETERS_PER_HOUR)).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:advisory", "123 mph"),
                StringMapEntryAdd("maxspeed:type:advisory", "sign")
            ),
            AdvisorySpeedSign(Speed(123, MILES_PER_HOUR)).appliedTo(mapOf())
        )
    }

    @Test fun `apply zone sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "AA:zone123")
            ),
            MaxSpeedZone("AA", Speed(123, KILOMETERS_PER_HOUR)).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "15 mph"),
                StringMapEntryAdd("maxspeed:type", "ZZ:zone15")
            ),
            MaxSpeedZone("ZZ", Speed(15, MILES_PER_HOUR)).appliedTo(mapOf())
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
            setOf(StringMapEntryAdd("maxspeed:type", "GB:nsl_restricted")),
            DefaultMaxSpeed("GB", RoadType.UnitedKingdom.RESTRICTED).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "GB:nsl_single")),
            DefaultMaxSpeed("GB", RoadType.UnitedKingdom.SINGLE).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "GB:nsl_dual")),
            DefaultMaxSpeed("GB", RoadType.UnitedKingdom.DUAL).appliedTo(mapOf())
        )
    }
}

private fun MaxSpeedAnswer.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
