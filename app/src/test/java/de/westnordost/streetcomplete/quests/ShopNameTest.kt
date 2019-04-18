package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.shop_name.AddShopName
import de.westnordost.streetcomplete.quests.shop_name.NoShopNameSign
import de.westnordost.streetcomplete.quests.shop_name.ShopName
import org.junit.Test

import org.mockito.Mockito.mock

class ShopNameTest {

    private val questType = AddShopName(mock(OverpassMapDataDao::class.java))

    @Test
    fun `apply no name answer`() {
        questType.verifyAnswer(
            NoShopNameSign,
            StringMapEntryAdd("noname", "yes")
        )
    }

    @Test fun `apply name answer`() {
        questType.verifyAnswer(
            ShopName("Shop name here"),
            StringMapEntryAdd("name", "Shop name here")
        )
    }

}
