package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm

import org.mockito.Mockito.mock

class AddParkingFeeTest : AOsmElementQuestTypeTest() {

    override val questType = AddParkingFee(mock(OverpassMapDataDao::class.java))

    fun testYes() {
        bundle.putBoolean(AddParkingFeeForm.FEE, true)
        verify(StringMapEntryAdd("fee", "yes"))
    }

    fun testNo() {
        bundle.putBoolean(AddParkingFeeForm.FEE, false)
        verify(StringMapEntryAdd("fee", "no"))
    }

    fun testYesButOnlyAt() {
        bundle.putBoolean(AddParkingFeeForm.FEE, false)
        bundle.putString(AddParkingFeeForm.FEE_CONDITONAL_HOURS, "xyz")
        verify(
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("fee:conditional", "yes @ (xyz)")
        )
    }

    fun testYesButNotAt() {
        bundle.putBoolean(AddParkingFeeForm.FEE, true)
        bundle.putString(AddParkingFeeForm.FEE_CONDITONAL_HOURS, "xyz")
        verify(
            StringMapEntryAdd("fee", "yes"),
            StringMapEntryAdd("fee:conditional", "no @ (xyz)")
        )
    }
}
