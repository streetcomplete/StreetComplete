package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mock

class AddBuildingLevelsTest : AOsmElementQuestTypeTest() {

    override val questType = AddBuildingLevels(mock(OverpassMapDataDao::class.java))

    @Test fun buildingLevelsOnly() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        verify(
            StringMapEntryAdd("building:levels", "5")
        )
    }

    @Test fun buildingLevelsAndZeroRoofLevels() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 0)
        verify(
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "0")
        )
    }

    @Test fun buildingLevelsAndRoofLevels() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 3)
        verify(
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "3")
        )
    }
}
