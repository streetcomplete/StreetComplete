package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.max_speed.*
import org.junit.Test

import org.mockito.Mockito.mock

class AddMaxSpeedTest {

    private val questType = AddMaxSpeed(mock(OverpassMapDataDao::class.java))

    @Test fun `apply no sign answer`() {
        questType.verifyAnswer(
            ImplicitMaxSpeed("XX", "flubberway"),
            StringMapEntryAdd("maxspeed:type", "XX:flubberway")
        )
    }

    @Test fun `apply sign answer`() {
        questType.verifyAnswer(
            MaxSpeedSign("123"),
            StringMapEntryAdd("maxspeed", "123"),
            StringMapEntryAdd("maxspeed:type", "sign")
        )
    }

    @Test fun `apply advisory sign answer`() {
        questType.verifyAnswer(
            AdvisorySpeedSign("123"),
            StringMapEntryAdd("maxspeed:advisory", "123"),
            StringMapEntryAdd("maxspeed:type:advisory", "sign")
        )
    }

    @Test fun `apply zone sign answer`() {
        questType.verifyAnswer(
            MaxSpeedZone("123", "AA", "zoneXYZ"),
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
