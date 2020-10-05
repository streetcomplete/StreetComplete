package de.westnordost.streetcomplete.quests.road_name.data

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoadNameSuggestionsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: RoadNameSuggestionsDao

    @Before fun createDao() {
        dao = RoadNameSuggestionsDao(dbHelper, serializer)
    }

    @Test fun getNoNames() {
        val result = dao.getNames(listOf(OsmLatLon(5.0, 5.0)), 0.0)
        assertEquals(0, result.size)
    }

    @Test fun getOneNames() {
        val names = mapOf(
            "de" to "Große Straße",
            "en" to "Big Street"
        )
        dao.putRoad(1, names, listOf(OsmLatLon(0.0, 0.0), OsmLatLon(0.0, 0.0001)))

        val result = dao.getNames(listOf(OsmLatLon(0.0, 0.00005)), 1000.0)

        assertEquals(listOf(names), result)
    }

    @Test fun getMultipleNamesSortedByDistance() {
        val middle = mapOf("en" to "Middle Street")
        dao.putRoad(1, middle, listOf(OsmLatLon(0.0, 0.0001), OsmLatLon(0.0, 0.0002)))

        val far = mapOf("en" to "Far Street")
        dao.putRoad(2, far, listOf(OsmLatLon(0.0, 0.0002), OsmLatLon(0.0, 0.0003)))

        val near = mapOf("en" to "Near Street")
        dao.putRoad(3, near, listOf(OsmLatLon(0.0, 0.0000), OsmLatLon(0.0, 0.0001)))

        val tooFar = mapOf("en" to "Too Far Street")
        dao.putRoad(4, tooFar, listOf(OsmLatLon(10.0, 0.0002), OsmLatLon(10.0, 0.0003)))

        val result = dao.getNames(listOf(OsmLatLon(0.0, 0.0)), 1000.0)
        assertEquals(listOf(near, middle, far), result)
    }
}
