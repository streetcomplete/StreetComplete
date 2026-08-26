package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BuildingUseTypeParserKtTest {

    @Test fun `parse missing building use`() {
        assertNull(createBuildingUseType(mapOf()))
        assertNull(createBuildingUseType(mapOf("building" to "office")))
    }

    @Test fun `parse building use`() {
        assertEquals(
            OFFICE,
            createBuildingUseType(mapOf(
                "building" to "commercial",
                "building:use" to "office"
            ))
        )

        assertEquals(
            APARTMENTS,
            createBuildingUseType(mapOf(
                "building" to "residential",
                "building:use" to "apartments"
            ))
        )
    }

    @Test fun `parse building use aliases`() {
        assertEquals(
            FARM_AUXILIARY,
            createBuildingUseType(mapOf("building:use" to "livestock"))
        )

        assertEquals(
            RELIGIOUS,
            createBuildingUseType(mapOf("building:use" to "convent"))
        )
    }

    @Test fun `parse houses with alternative tagging scheme`() {
        assertEquals(
            HOUSE,
            createBuildingUseType(mapOf("building:use" to "house"))
        )

        assertEquals(
            BUNGALOW,
            createBuildingUseType(mapOf(
                "building:use" to "house",
                "house" to "bungalow"
            ))
        )

        assertEquals(
            DETACHED,
            createBuildingUseType(mapOf(
                "building:use" to "house",
                "house" to "detached"
            ))
        )
    }

    @Test fun `parse excluded building uses`() {
        assertNull(
            createBuildingUseType(mapOf("building:use" to "ruins"))
        )

        assertNull(
            createBuildingUseType(mapOf("building:use" to "abandoned"))
        )

        assertNull(
            createBuildingUseType(mapOf("building:use" to "historic"))
        )
    }

    @Test fun `parse unsupported building use`() {
        assertNull(
            createBuildingUseType(mapOf("building:use" to "something else"))
        )
    }
}
