package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm

import org.mockito.Mockito.mock

class AddBuildingLevelsTest : AOsmElementQuestTypeTest() {

    override val questType = AddBuildingLevels(mock(OverpassMapDataDao::class.java))

    override fun setUp() {
        super.setUp()
        tags["building"] = "residential"
    }

    fun testBuildingLevelsOnly() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        verify(
            StringMapEntryAdd("building:levels", "5")
        )
    }

    fun testBuildingLevelsAndZeroRoofLevels() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 0)
        verify(
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "0")
        )
    }

    fun testBuildingLevelsAndRoofLevels() {
        bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5)
        bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 3)
        verify(
            StringMapEntryAdd("building:levels", "5"),
            StringMapEntryAdd("roof:levels", "3")
        )
    }
}
