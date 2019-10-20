package de.westnordost.streetcomplete.quests.localized_name.data

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
        dao.putRoad(1, names, createRoadPositions())

        val result = dao.getNames(createPosOnRoad(), 1000.0)

        assertEquals(listOf(names), result)
    }

    @Test fun getMultipleNames() {
        val names1 = mapOf("en" to "Big Street")
        dao.putRoad(1, names1, createRoadPositions())

        val names2 = mapOf("es" to "Calle Pequena")
        dao.putRoad(2, names2, createRoadPositions())

        val result = dao.getNames(createPosOnRoad(), 1000.0)
        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(names1, names2)))
    }

    private fun createRoadPositions() = listOf(OsmLatLon(0.0, 0.0), OsmLatLon(0.0, 0.0001))

    private fun createPosOnRoad() = listOf(OsmLatLon(0.0, 0.00005))
}
