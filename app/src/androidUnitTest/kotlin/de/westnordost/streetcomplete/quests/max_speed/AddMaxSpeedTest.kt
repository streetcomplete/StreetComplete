package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMaxSpeedTest {

    private val questType = AddMaxSpeed()

    @Test fun `apply no sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxspeed:type", "XX:flubberway")),
            questType.answerApplied(ImplicitMaxSpeed("XX", "flubberway", null))
        )
    }

    @Test fun `apply sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            questType.answerApplied(MaxSpeedSign(Kmh(123)))
        )
    }

    @Test fun `apply mph sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123 mph"),
                StringMapEntryAdd("maxspeed:type", "sign")
            ),
            questType.answerApplied(MaxSpeedSign(Mph(123)))
        )
    }

    @Test fun `apply advisory sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:advisory", "123"),
                StringMapEntryAdd("maxspeed:type:advisory", "sign")
            ),
            questType.answerApplied(AdvisorySpeedSign(Kmh(123)))
        )
    }

    @Test fun `apply zone sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed", "123"),
                StringMapEntryAdd("maxspeed:type", "AA:zoneXYZ")
            ),
            questType.answerApplied(MaxSpeedZone(Kmh(123), "AA", "zoneXYZ"))
        )
    }

    @Test fun `apply living street answer`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "residential", "living_street")),
            questType.answerAppliedTo(IsLivingStreet, mapOf("highway" to "residential"))
        )
    }

    @Test fun `apply nsl restricted answer lit mapped`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "GB:nsl_restricted"),
                StringMapEntryModify("lit", "yes", "yes")
            ),
            questType.answerAppliedTo(
                ImplicitMaxSpeed("GB", "nsl_restricted", true),
                mapOf("lit" to "yes")
            )
        )
    }

    @Test fun `apply nsl restricted answer lit not mapped`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "GB:nsl_restricted"),
                StringMapEntryAdd("lit", "yes")
            ),
            questType.answerApplied(
                ImplicitMaxSpeed("GB", "nsl_restricted", true),
            )
        )
    }

    @Test fun `apply nsl single answer lit mapped`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "GB:nsl_single"),
                StringMapEntryModify("lit", "no", "no")
            ),
            questType.answerAppliedTo(
                ImplicitMaxSpeed("GB", "nsl_single", false),
                mapOf("lit" to "no")
            )
        )
    }

    @Test fun `apply nsl single answer lit not mapped`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "GB:nsl_single"),
                StringMapEntryAdd("lit", "no")
            ),
            questType.answerApplied(ImplicitMaxSpeed("GB", "nsl_single", false))
        )
    }

    @Test fun `apply nsl dual answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxspeed:type", "GB:nsl_dual")
            ),
            questType.answerApplied(ImplicitMaxSpeed("GB", "nsl_dual", null))
        )
    }
}
