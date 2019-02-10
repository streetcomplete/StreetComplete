package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.foot.AddAccessibleForPedestrians
import de.westnordost.streetcomplete.quests.foot.AccessibleForPedestriansAnswer.*
import org.junit.Test
import org.mockito.Mockito.mock

class AddAccessibleForPedestriansTest {

	private val questType = AddAccessibleForPedestrians(mock(OverpassMapDataDao::class.java))

	@Test fun `apply yes answer`() {
		questType.verifyAnswer(YES, StringMapEntryAdd("foot", "yes"))
	}

	@Test fun `apply no answer`() {
        questType.verifyAnswer(NO, StringMapEntryAdd("foot", "no"))
	}

	@Test fun `apply living street answer`() {
        questType.verifyAnswer(
            mapOf("highway" to "residential"),
            IS_LIVING_STREET,
            StringMapEntryModify("highway", "residential", "living_street")
        )
	}
}
