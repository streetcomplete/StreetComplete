package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

import org.mockito.Mockito.mock

class AddRoadNameTest {

    private val questType = AddRoadName(
        mock(OverpassMapDataDao::class.java),
        mock(RoadNameSuggestionsDao::class.java),
        mock(PutRoadNameSuggestionsHandler::class.java)
    )

    private val tags = mapOf("highway" to "residential")

    @Test fun `apply no name answer`() {
        questType.verifyAnswer(
            tags,
            NoRoadName,
            StringMapEntryAdd("noname", "yes")
        )
    }

    @Test fun `apply name answer with one name`() {
        questType.verifyAnswer(
            tags,
            roadName(LocalizedName("", "my name")),
            StringMapEntryAdd("name", "my name")
        )
    }

    @Test fun `apply name answer with multiple names`() {
        questType.verifyAnswer(
            tags,
            roadName(
                LocalizedName("", "Altona / All-Too-Close"),
                LocalizedName("de", "Altona"),
                LocalizedName("en", "All-Too-Close")
            ),
            StringMapEntryAdd("name", "Altona / All-Too-Close"),
            StringMapEntryAdd("name:en", "All-Too-Close"),
            StringMapEntryAdd("name:de", "Altona")
        )
    }

    @Test fun `apply is service road answer`() {
        questType.verifyAnswer(
            tags,
            RoadIsServiceRoad,
            StringMapEntryModify("highway", tags["highway"], "service")
        )
    }

    @Test fun `apply is track answer`() {
        questType.verifyAnswer(
            tags,
            RoadIsTrack,
            StringMapEntryModify("highway", tags["highway"], "track")
        )
    }

    @Test fun `apply is link answer`() {

        for (highway in sequenceOf("primary", "secondary", "tertiary")) {
            questType.verifyAnswer(
                mapOf("highway" to highway),
                RoadIsLinkRoad,
                StringMapEntryModify("highway", highway, "${highway}_link")
            )
        }
    }

    // convenience method
    private fun roadName(vararg names:LocalizedName): RoadName {
        val pointsList = listOf(listOf(OsmLatLon(0.0,0.0), OsmLatLon(1.0,1.0)))
        val geometry = ElementPolylinesGeometry(pointsList, OsmLatLon(0.0, 0.0))
        return RoadName(names.toList(), 1L, geometry)
    }
}
