package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeedForm

import org.mockito.Mockito.mock

class AddMaxSpeedTest : AOsmElementQuestTypeTest() {

    override val questType = AddMaxSpeed(mock(OverpassMapDataDao::class.java))

    fun testNoSign() {
        bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "flubberway")
        bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "XX")
        verify(StringMapEntryAdd(MAXSPEED_TYPE, "XX:flubberway"))
    }

    fun testNormalSign() {
        bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123")
        verify(
            StringMapEntryAdd("maxspeed", "123"),
            StringMapEntryAdd(MAXSPEED_TYPE, "sign")
        )
    }

    fun testAdvisoryNormalSign() {
        bundle.putString(AddMaxSpeedForm.ADVISORY_SPEED, "123")
        verify(
            StringMapEntryAdd("maxspeed:advisory", "123"),
            StringMapEntryAdd("$MAXSPEED_TYPE:advisory", "sign")
        )
    }

    fun testZoneSign() {
        bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123")
        bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "zoneXYZ")
        bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "AA")
        verify(
            StringMapEntryAdd("maxspeed", "123"),
            StringMapEntryAdd(MAXSPEED_TYPE, "AA:zoneXYZ")
        )
    }

    fun testLivingStreet() {
        tags["highway"] = "residential"
        bundle.putBoolean(AddMaxSpeedForm.LIVING_STREET, true)
        verify(StringMapEntryModify("highway", "residential", "living_street"))
    }

    companion object {
        private val MAXSPEED_TYPE = "maxspeed:type"
    }
}
