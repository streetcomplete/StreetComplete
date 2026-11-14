package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BuildingTypeAndStatusParserKtTest {

    @Test fun `parse not buildings`() {
        assertEquals(null, parse().type)
        assertEquals(null, parse("bla" to "blub").type)
        assertEquals(null, parse("building" to "no").type)
        assertEquals(null, parse("building" to "entrance").type)
    }

    @Test fun `parse buildings`() {
        assertEquals(HUT, parse("building" to "hut").type)
        assertEquals(STORAGE_TANK, parse("man_made" to "storage_tank").type)
        assertEquals(null, parse("building" to "yes").type)
    }

    @Test fun `parse unsupported buildings`() {
        assertEquals(UNSUPPORTED, parse("building" to "something else").type)
        assertEquals(UNSUPPORTED, parse("man_made" to "something else").type)
        assertEquals(UNSUPPORTED, parse("building" to "house", "house" to "something else").type)
    }

    @Test fun `parse deprecated building aliases`() {
        assertEquals(null, parse("building" to "semi").type)
        assertEquals(null, parse("building" to "abandoned").type)
    }

    @Test fun `parse non deprecated building aliases`() {
        assertEquals(FARM_AUXILIARY, parse("building" to "livestock").type)
        assertEquals(RELIGIOUS, parse("building" to "convent").type)
        assertEquals(TOWER, parse("building" to "lighthouse").type)
    }

    @Test fun `parse houses with alternative tagging scheme`() {
        assertEquals(HOUSE, parse("building" to "house").type)
        assertEquals(BUNGALOW, parse("building" to "house", "house" to "bungalow").type)
        assertEquals(DETACHED, parse("building" to "house", "house" to "detached").type)
        assertEquals(SEMI_DETACHED, parse("building" to "house", "house" to "semi-detached").type)
        assertEquals(TERRACE, parse("building" to "house", "house" to "terrace").type)
        assertEquals(TERRACED_HOUSE, parse("building" to "house", "house" to "terraced").type)
    }

    @Test fun `parse lifecycle`() {
        assertEquals(CONSTRUCTION, parse("construction" to "yes").status)
        assertEquals(CONSTRUCTION, parse("construction:building" to "house").status)

        assertEquals(DISUSED, parse("disused" to "yes").status)
        assertEquals(DISUSED, parse("disused:building" to "house").status)

        assertEquals(ABANDONED, parse("abandoned" to "yes").status)
        assertEquals(ABANDONED, parse("abandoned:building" to "house").status)

        assertEquals(RUINS, parse("ruins" to "yes").status)
        assertEquals(RUINS, parse("ruins:building" to "house").status)
    }

    @Test fun `parse use`() {
        assertEquals(APARTMENTS, parse("building:use" to "apartments").status)
        // alias
        assertEquals(BUNGALOW, parse("building:use" to "cottage").status)
        // unsupported
        assertEquals(UNSUPPORTED, parse("building:use" to "something else").status)
        // invalid / empty
        assertEquals(null, parse("building:use" to "fixme").status)
        assertEquals(null, parse("building:use" to "yes").status)
        // house
        assertEquals(BUNGALOW, parse("building:use" to "house", "house" to "bungalow").status)
        assertEquals(BUNGALOW, parse("building:use" to "bungalow").status)
        // for man made
        assertEquals(TOWER, parse("man_made:use" to "tower").status)
    }

    @Test fun `parse construction`() {
        // none specified
        assertEquals(
            BuildingTypeAndStatus(null, CONSTRUCTION),
            parse("building" to "construction")
        )
        // invalid / empty specified
        assertEquals(
            BuildingTypeAndStatus(null, CONSTRUCTION),
            parse("building" to "construction", "construction" to "yes")
        )
        assertEquals(
            BuildingTypeAndStatus(null, CONSTRUCTION),
            parse("building" to "construction", "construction" to "fixme")
        )
        // house
        assertEquals(
            BuildingTypeAndStatus(BUNGALOW, CONSTRUCTION),
            parse("building" to "construction", "construction" to "bungalow")
        )
        assertEquals(
            BuildingTypeAndStatus(BUNGALOW, CONSTRUCTION),
            parse("building" to "construction", "construction" to "house", "house" to "bungalow")
        )
        // for man made: not possible
        assertEquals(
            BuildingTypeAndStatus(UNSUPPORTED, CONSTRUCTION),
            parse("building" to "construction", "construction" to "tower")
        )
    }
}

private fun parse(vararg tags: Pair<String, String>): BuildingTypeAndStatus =
    createBuildingTypeAndStatus(mapOf(*tags))
