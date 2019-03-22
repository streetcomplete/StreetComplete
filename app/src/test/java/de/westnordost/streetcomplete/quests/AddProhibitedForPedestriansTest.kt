package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.*
import org.junit.Test
import org.mockito.Mockito.mock

class AddProhibitedForPedestriansTest {

	private val questType = AddProhibitedForPedestrians(mock(OverpassMapDataDao::class.java))

	@Test fun `apply yes answer`() {
		questType.verifyAnswer(YES, StringMapEntryAdd("foot", "no"))
	}

	@Test fun `apply no answer`() {
        questType.verifyAnswer(NO, StringMapEntryAdd("foot", "yes"))
	}

    @Test fun `apply separate sidewalk answer`() {
        questType.verifyAnswer(
            mapOf("sidewalk" to "no"),
            HAS_SEPARATE_SIDEWALK,
            StringMapEntryAdd("foot", "use_sidepath"),
            StringMapEntryModify("sidewalk", "no", "separate")
        )
    }

	@Test fun `apply living street answer`() {
        questType.verifyAnswer(
            mapOf("highway" to "residential"),
            IS_LIVING_STREET,
            StringMapEntryModify("highway", "residential", "living_street")
        )
	}
}
