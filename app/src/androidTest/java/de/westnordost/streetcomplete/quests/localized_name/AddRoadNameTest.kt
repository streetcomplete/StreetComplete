package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mock

class AddRoadNameTest : AOsmElementQuestTypeTest() {

    override val questType = AddRoadName(
        mock(OverpassMapDataDao::class.java),
        mock(RoadNameSuggestionsDao::class.java),
        mock(PutRoadNameSuggestionsHandler::class.java)
    )

    @Before fun setupTags() {
        tags["highway"] = "residential"
    }

    @Test fun noName() {
        bundle.putBoolean(AddLocalizedNameForm.NO_NAME, true)
        verify(StringMapEntryAdd("noname", "yes"))
    }

    @Test fun oneName() {
        bundle.putStringArray(AddLocalizedNameForm.NAMES, arrayOf("my name"))
        bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, arrayOf(""))
        verify(StringMapEntryAdd("name", "my name"))
    }

    @Test fun multipleNames() {
        bundle.putStringArray(AddLocalizedNameForm.NAMES, arrayOf("my name", "kröötz"))
        bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, arrayOf("en", "de"))
        verify(
            StringMapEntryAdd("name", "my name"),
            StringMapEntryAdd("name:en", "my name"),
            StringMapEntryAdd("name:de", "kröötz")
        )
    }

    @Test fun multipleNamesDefaultNameIsOfNoSpecificLanguage() {
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

    @Test fun isService() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_SERVICE)
        verify(StringMapEntryModify("highway", tags["highway"], "service"))
    }

    @Test fun isTrack() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_TRACK)
        verify(StringMapEntryModify("highway", tags["highway"], "track"))
    }

    @Test fun isLink() {
        bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_LINK)

        tags["highway"] = "primary"
        verify(StringMapEntryModify("highway", tags["highway"], "primary_link"))

        tags["highway"] = "secondary"
        verify(StringMapEntryModify("highway", tags["highway"], "secondary_link"))

        tags["highway"] = "tertiary"
        verify(StringMapEntryModify("highway", tags["highway"], "tertiary_link"))
    }
}
