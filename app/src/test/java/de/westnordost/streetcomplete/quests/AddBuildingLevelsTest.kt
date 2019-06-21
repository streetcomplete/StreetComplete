package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_levels.BuildingLevelsAnswer
import org.junit.Test

import org.mockito.Mockito.mock

class AddBuildingLevelsTest {

    private val questType = AddBuildingLevels(mock(OverpassMapDataDao::class.java))

    @Test fun `apply building levels answer`() {
        questType.verifyAnswer(
            BuildingLevelsAnswer(5, null),
            StringMapEntryAdd("building:levels", "5")
        )
    }

    @Test fun `apply building levels and zero roof levels answer`() {
        questType.verifyAnswer(
            BuildingLevelsAnswer(5, 0),
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "0")
        )
    }

    @Test fun `apply building and roof levels answer`() {
        questType.verifyAnswer(
            BuildingLevelsAnswer(5, 3),
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "3")
        )
    }
}
