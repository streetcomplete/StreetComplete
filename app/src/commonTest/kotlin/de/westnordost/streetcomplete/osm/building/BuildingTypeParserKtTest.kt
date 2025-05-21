package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildingTypeParserKtTest {

    @Test fun `parse not buildings`() {
        assertEquals(null, createBuildingType(mapOf()))
        assertEquals(null, createBuildingType(mapOf("bla" to "blub")))
        assertEquals(null, createBuildingType(mapOf("building" to "no")))
        assertEquals(null, createBuildingType(mapOf("building" to "entrance")))
    }

    @Test fun `parse buildings`() {
        assertEquals(HUT, createBuildingType(mapOf("building" to "hut")))
        assertEquals(STORAGE_TANK, createBuildingType(mapOf("man_made" to "storage_tank")))
        assertEquals(HISTORIC, createBuildingType(mapOf("historic" to "yes")))

        assertEquals(null, createBuildingType(mapOf("building" to "yes")))
        assertEquals(UNSUPPORTED, createBuildingType(mapOf("building" to "something else")))
        assertEquals(UNSUPPORTED, createBuildingType(mapOf("man_made" to "something else")))
    }

    @Test fun `parse deprecated building aliases`() {
        assertEquals(null, createBuildingType(mapOf("building" to "semi")))
        assertEquals(null, createBuildingType(mapOf("building" to "abandoned")))
    }

    @Test fun `parse non deprecated building aliases`() {
        assertEquals(FARM_AUXILIARY, createBuildingType(mapOf("building" to "livestock")))
        assertEquals(RELIGIOUS, createBuildingType(mapOf("building" to "convent")))
    }

    @Test fun `parse houses with alternative tagging scheme`() {
        assertEquals(HOUSE, createBuildingType(mapOf("building" to "house")))
        assertEquals(BUNGALOW, createBuildingType(mapOf("building" to "house", "house" to "bungalow")))
        assertEquals(DETACHED, createBuildingType(mapOf("building" to "house", "house" to "detached")))
        assertEquals(HOUSE, createBuildingType(mapOf("building" to "house", "house" to "something else")))
    }

    @Test fun `parse historic is secondary`() {
        assertEquals(HISTORIC, createBuildingType(mapOf("historic" to "yes")))
        assertEquals(HISTORIC, createBuildingType(mapOf("building" to "yes", "historic" to "yes")))
        assertEquals(APARTMENTS, createBuildingType(mapOf("building" to "apartments", "historic" to "yes")))
    }
}
