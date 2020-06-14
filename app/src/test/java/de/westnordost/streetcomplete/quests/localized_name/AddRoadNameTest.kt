package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddRoadNameTest {

    private val questType = AddRoadName(mock(), mock())

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

    @Test fun `apply ref answer`() {
        val refs = listOf("9","A","A2","L 3211","US-9","MEX 25","1234","G9321")
        for (ref in refs) {
            questType.verifyAnswer(
                tags,
                roadName(LocalizedName("", ref)),
                StringMapEntryAdd("ref", ref)
            )
        }
    }

    @Test fun `do not apply ref answer if it is a localized name`() {
        questType.verifyAnswer(
            tags,
            roadName(
                LocalizedName("", "A1"),
                LocalizedName("de", "A1")
            ),
            StringMapEntryAdd("name", "A1"),
            StringMapEntryAdd("name:de", "A1")
        )
        questType.verifyAnswer(
            tags,
            roadName(LocalizedName("de", "A1")),
            StringMapEntryAdd("name:de", "A1")
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
            StringMapEntryModify("highway", tags.getValue("highway"), "service")
        )
    }

    @Test fun `apply is track answer`() {
        questType.verifyAnswer(
            tags,
            RoadIsTrack,
            StringMapEntryModify("highway", tags.getValue("highway"), "track")
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
