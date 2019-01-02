package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddCollectionTimesForm
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes

import org.mockito.Mockito.mock

class AddPostboxCollectionTimesTest : AOsmElementQuestTypeTest() {

    override val questType = AddPostboxCollectionTimes(mock(OverpassMapDataDao::class.java))

    fun testNoTimes() {
        bundle.putBoolean(AddCollectionTimesForm.NO_TIMES_SPECIFIED, true)
        verify(StringMapEntryAdd("collection_times:signed", "no"))
    }

    fun testTimes() {
        bundle.putString(AddCollectionTimesForm.TIMES, "my times")
        verify(StringMapEntryAdd("collection_times", "my times"))
    }

}
