package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkAnswer
import org.junit.Test
import org.mockito.Mockito.mock

class AddSidewalkTest {

	private val questType = AddSidewalk(mock(OverpassMapDataDao::class.java))

	@Test fun `apply no sidewalk answer`() {
		questType.verifyAnswer(
			SidewalkAnswer(left = false, right = false),
			StringMapEntryAdd("sidewalk", "none")
		)
	}

	@Test fun `apply sidewalk left answer`() {
		questType.verifyAnswer(
			SidewalkAnswer(left = true, right = false),
			StringMapEntryAdd("sidewalk", "left")
		)
	}

	@Test fun `apply sidewalk right answer`() {
		questType.verifyAnswer(
			SidewalkAnswer(left = false, right = true),
			StringMapEntryAdd("sidewalk", "right")
		)
	}

	@Test fun `apply sidewalk on both sides answer`() {
		questType.verifyAnswer(
			SidewalkAnswer(left = true, right = true),
			StringMapEntryAdd("sidewalk", "both")
		)
	}
}
