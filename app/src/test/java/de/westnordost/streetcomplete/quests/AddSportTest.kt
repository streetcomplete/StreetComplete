package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.sport.AddSport
import org.junit.Test

import org.mockito.Mockito.mock

class AddSportTest {

    private val questType = AddSport(mock(OverpassMapDataDao::class.java))

    @Test fun `replace hockey when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "hockey"),
            listOf("field_hockey"),
            StringMapEntryModify("sport", "hockey", "field_hockey")
        )
    }

    @Test fun `replace team handball when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "team_handball"),
            listOf("handball"),
            StringMapEntryModify("sport", "team_handball", "handball")
        )
    }

    @Test fun `replace skating when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "skating"),
            listOf("ice_skating"),
            StringMapEntryModify("sport", "skating", "ice_skating")
        )
    }

    @Test fun `replace football when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "football"),
            listOf("soccer"),
            StringMapEntryModify("sport", "football", "soccer")
        )
    }

    @Test fun `apply sport answer`() {
        questType.verifyAnswer(
            listOf("soccer"),
            StringMapEntryAdd("sport", "soccer")
        )
    }
}
