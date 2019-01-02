package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao

import org.mockito.Mockito.mock

class AddRoadNameTest : AOsmElementQuestTypeTest() {

    override val questType = AddRoadName(
        mock(OverpassMapDataDao::class.java),
        mock(RoadNameSuggestionsDao::class.java),
        mock(PutRoadNameSuggestionsHandler::class.java)
    )

    override fun setUp() {
        super.setUp()
        tags["highway"] = "residential"
    }

    fun testNoName() {
        bundle.putBoolean(AddLocalizedNameForm.NO_NAME, true)
        verify(StringMapEntryAdd("noname", "yes"))
    }

    fun testOneName() {
        bundle.putStringArray(AddLocalizedNameForm.NAMES, arrayOf("my name"))
        verify(StringMapEntryAdd("name", "my name"))
    }

    fun testMultipleNames() {
        bundle.putStringArray(AddLocalizedNameForm.NAMES, arrayOf("my name", "kröötz"))
        bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, arrayOf("en", "de"))
        verify(
            StringMapEntryAdd("name", "my name"),
            StringMapEntryAdd("name:en", "my name"),
            StringMapEntryAdd("name:de", "kröötz")
        )
    }

    fun testMultipleNamesDefaultNameIsOfNoSpecificLanguage() {
        bundle.putStringArray(
            AddLocalizedNameForm.NAMES,
            arrayOf("my name / kröötz", "my name", "kröötz")
        )
        bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, arrayOf("", "en", "de"))
        verify(
            StringMapEntryAdd("name", "my name / kröötz"),
            StringMapEntryAdd("name:en", "my name"),
            StringMapEntryAdd("name:de", "kröötz")
        )
    }

    fun testIsService() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_SERVICE)
        verify(StringMapEntryModify("highway", tags["highway"], "service"))
    }

    fun testIsTrack() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_TRACK)
        verify(StringMapEntryModify("highway", tags["highway"], "track"))
    }

    fun testIsLink() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_LINK)

        tags["highway"] = "primary"
        verify(StringMapEntryModify("highway", tags["highway"], "primary_link"))

        tags["highway"] = "secondary"
        verify(StringMapEntryModify("highway", tags["highway"], "secondary_link"))

        tags["highway"] = "tertiary"
        verify(StringMapEntryModify("highway", tags["highway"], "tertiary_link"))
    }
}
