package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.sport.Sport.*
import org.junit.Test

class AddSportTest {

    private val questType = AddSport()

    @Test fun `replace hockey when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "hockey"),
            listOf(FIELD_HOCKEY),
            StringMapEntryModify("sport", "hockey", "field_hockey")
        )
    }

    @Test fun `replace team handball when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "team_handball"),
            listOf(HANDBALL),
            StringMapEntryModify("sport", "team_handball", "handball")
        )
    }

    @Test fun `replace skating when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "skating"),
            listOf(ICE_SKATING),
            StringMapEntryModify("sport", "skating", "ice_skating")
        )
    }

    @Test fun `replace football when applying answer`() {
        questType.verifyAnswer(
            mapOf("sport" to "football"),
            listOf(SOCCER),
            StringMapEntryModify("sport", "football", "soccer")
        )
    }

    @Test fun `apply sport answer`() {
        questType.verifyAnswer(
            listOf(SOCCER),
            StringMapEntryAdd("sport", "soccer")
        )
    }
}
