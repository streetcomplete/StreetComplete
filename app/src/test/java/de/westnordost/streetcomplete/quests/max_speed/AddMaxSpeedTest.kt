package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import kotlin.test.Test

class AddMaxSpeedTest {

    private val questType = AddMaxSpeed()

    @Test fun `apply no sign answer`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("XX", "flubberway", null),
            StringMapEntryAdd("maxspeed:type", "XX:flubberway")
        )
    }

    @Test fun `apply sign answer`() {
        questType.verifyAnswer(
            MaxSpeedSign(Kmh(123)),
            StringMapEntryAdd("maxspeed", "123"),
            StringMapEntryAdd("maxspeed:type", "sign")
        )
    }

    @Test fun `apply mph sign answer`() {
        questType.verifyAnswer(
            MaxSpeedSign(Mph(123)),
            StringMapEntryAdd("maxspeed", "123 mph"),
            StringMapEntryAdd("maxspeed:type", "sign")
        )
    }

    @Test fun `apply advisory sign answer`() {
        questType.verifyAnswer(
            AdvisorySpeedSign(Kmh(123)),
            StringMapEntryAdd("maxspeed:advisory", "123"),
            StringMapEntryAdd("maxspeed:type:advisory", "sign")
        )
    }

    @Test fun `apply zone sign answer`() {
        questType.verifyAnswer(
            MaxSpeedZone(Kmh(123), "AA", "zoneXYZ"),
            StringMapEntryAdd("maxspeed", "123"),
            StringMapEntryAdd("maxspeed:type", "AA:zoneXYZ")
        )
    }

    @Test fun `apply living street answer`() {
        questType.verifyAnswer(
            mapOf("highway" to "residential"),
            IsLivingStreet,
            StringMapEntryModify("highway", "residential", "living_street")
        )
    }

    @Test fun `apply nsl restricted answer lit mapped`() {
        questType.verifyAnswer(
            mapOf("lit" to "yes"),
            ImplicitMaxSpeed("GB", "nsl_restricted", true),
            StringMapEntryAdd("maxspeed:type", "GB:nsl_restricted"),
            StringMapEntryModify("lit", "yes", "yes")
        )
    }

    @Test fun `apply nsl restricted answer lit not mapped`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("GB", "nsl_restricted", true),
            StringMapEntryAdd("maxspeed:type", "GB:nsl_restricted"),
            StringMapEntryAdd("lit", "yes")
        )
    }

    @Test fun `apply nsl single answer lit mapped`() {
        questType.verifyAnswer(
            mapOf("lit" to "no"),
            ImplicitMaxSpeed("GB", "nsl_single", false),
            StringMapEntryAdd("maxspeed:type", "GB:nsl_single"),
            StringMapEntryModify("lit", "no", "no")
        )
    }

    @Test fun `apply nsl single answer lit not mapped`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("GB", "nsl_single", false),
            StringMapEntryAdd("maxspeed:type", "GB:nsl_single"),
            StringMapEntryAdd("lit", "no")
        )
    }

    @Test fun `apply nsl dual answer`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("GB", "nsl_dual", null),
            StringMapEntryAdd("maxspeed:type", "GB:nsl_dual")
        )
    }
}
