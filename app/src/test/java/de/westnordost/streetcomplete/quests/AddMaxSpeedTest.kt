package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.max_speed.*
import org.junit.Test

class AddMaxSpeedTest {

    private val questType = AddMaxSpeed(mock())

    @Test fun `apply no sign answer`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("XX", "flubberway"),
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
}
