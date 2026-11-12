package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class BuildingTypeCreatorKtTest {

    @Test fun `set building as unsupported not possible`() {
        assertFails { UNSUPPORTED.appliedTo(mapOf()) }
    }

    @Test fun `set building`() {
        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf())
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf("building" to "yes"))
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf("man_made" to "storage_tank"))
        )
    }
    @Test fun `set terraced house`() {
        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            TERRACED_HOUSE.appliedTo(mapOf())
        )

        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            TERRACED_HOUSE.appliedTo(mapOf(
                "building" to "house",
                "house" to "detached",
            ))
        )
    }
    @Test fun `set man-made`() {
        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo(mapOf())
        )

        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo(mapOf("man_made" to "silo"))
        )

        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo(mapOf("building" to "roof"))
        )
    }

    @Test fun `remove ruins etc`() {
        assertEquals(
            mapOf("building" to "roof", "historic" to "yes"),
            ROOF.appliedTo(mapOf(
                "building" to "residential",
                "ruins" to "yes",
                "abandoned" to "yes",
                "disused" to "yes",
                "historic" to "yes",
            ))
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf("building" to "residential", "ruins" to "yes"))
        )
    }

    @Test fun `does not remove historic when retagging historic building`() {
        assertEquals(
            mapOf("building" to "residential", "historic" to "yes"),
            RESIDENTIAL.appliedTo(mapOf(
                "building" to "yes",
                "historic" to "yes"
            ))
        )
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office",
                "historic" to "yes"
            ),
            COMMERCIAL.appliedTo(
                mapOf("building" to "residential", "historic" to "yes"),
                OFFICE
            )
        )
    }

    @Test
    fun `preserve historic when changing building use`() {
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office",
                "historic" to "yes"
            ),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial", "historic" to "yes"),
                OFFICE
            )
        )
    }

    @Test fun `update check date`() {
        assertEquals(
            mapOf("building" to "residential", "check_date" to nowAsCheckDateString()),
            RESIDENTIAL.appliedTo(mapOf("building" to "residential"))
        )
        assertEquals(
            mapOf("man_made" to "silo", "check_date" to nowAsCheckDateString()),
            SILO.appliedTo(mapOf("man_made" to "silo"))
        )
        assertEquals(
            mapOf("building" to "hut", "abandoned" to "yes", "check_date" to nowAsCheckDateString()),
            ABANDONED.appliedTo(mapOf("building" to "hut", "abandoned" to "yes"))
        )
    }

    @Test fun `don't overwrite aliases`() {
        assertEquals(
            mapOf("building" to "livestock", "check_date" to nowAsCheckDateString()),
            FARM_AUXILIARY.appliedTo(mapOf("building" to "livestock"))
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf("building" to "livestock"))
        )
        // Sanity check for regression
        assertEquals(
            mapOf("building" to "livestock", "building:use" to "office"),
            FARM_AUXILIARY.appliedTo(
                mapOf("building" to "livestock"),
                OFFICE
            )
        )
    }

    @Test fun `set building use`() {
        assertEquals(
            mapOf("building" to "commercial", "building:use" to "office"),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial"),
                OFFICE
            )
        )

        assertEquals(
            mapOf("building" to "residential", "building:use" to "apartments"),
            RESIDENTIAL.appliedTo(
                mapOf("building" to "residential"),
                APARTMENTS
            )
        )
    }

    @Test fun `replace existing building use`() {
        assertEquals(
            mapOf("building" to "commercial", "building:use" to "office"),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial", "building:use" to "retail"),
                 OFFICE
            )
        )
    }

    @Test fun `remove redundant building use`() {
        assertEquals(
            mapOf("building" to "office"),
            OFFICE.appliedTo(
                mapOf("building" to "office", "building:use" to "retail"),
                OFFICE
            )
        )
        assertEquals(
            mapOf("building" to "office"),
            OFFICE.appliedTo(
                mapOf("building" to "house", "building:use" to "apartments"),
                OFFICE
            )
        )
    }

    @Test fun `set building use removes abandoned and ruins`() {
        assertEquals(
            mapOf("building" to "commercial", "building:use" to "office"),
            COMMERCIAL.appliedTo(
                mapOf(
                    "building" to "commercial",
                    "abandoned" to "yes",
                    "ruins" to "yes",
                    "disused" to "yes"
                ),
                OFFICE
            )
        )
    }

    @Test fun `selecting ruins and abandoned removes building use`() {
        assertEquals(
            mapOf("building" to "commercial", "abandoned" to "yes"),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial", "building:use" to "office"),
                ABANDONED
            )
        )
        assertEquals(
            mapOf("building" to "commercial", "ruins" to "yes"),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial", "building:use" to "office"),
                RUINS
            )
        )
    }

    @Test fun `update check date when combined selection is unchanged`() {
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office",
                "check_date" to nowAsCheckDateString()
            ),
            COMMERCIAL.appliedTo(
                mapOf("building" to "commercial", "building:use" to "office"),
                OFFICE
            )
        )
    }
}

private fun BuildingType.appliedTo(
    tags: Map<String, String>,
    buildingUse: BuildingType? = null
): Map<String, String> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, buildingUse)
    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}
