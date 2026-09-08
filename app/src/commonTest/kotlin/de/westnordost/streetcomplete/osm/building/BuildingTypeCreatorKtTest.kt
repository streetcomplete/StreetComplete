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
    }

    @Test fun `set building type to yes when tagging as historic building`() {
        assertEquals(
            mapOf("building" to "yes", "historic" to "yes"),
            HISTORIC.appliedTo(mapOf(
                "building" to "apartments",
                "historic" to "no"
            ))
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
    }


    @Test fun `set building use`() {
        assertEquals(
            mapOf("building" to "commercial", "building:use" to "office"),
            OFFICE.buildingUseAppliedTo(mapOf("building" to "commercial"))
        )

        assertEquals(
            mapOf("building" to "residential", "building:use" to "apartments"),
            APARTMENTS.buildingUseAppliedTo(mapOf("building" to "residential"))
        )
    }

    @Test fun `replace existing building use`() {
        assertEquals(
            mapOf("building" to "commercial", "building:use" to "office"),
            OFFICE.buildingUseAppliedTo(mapOf("building" to "commercial", "building:use" to "retail"))
        )
    }

    @Test fun `remove redundant building use`() {
        assertEquals(
            mapOf("building" to "office"),
            OFFICE.buildingUseAppliedTo(mapOf("building" to "office", "building:use" to "office"))
        )
    }

    @Test fun `update check date for building use`() {
        assertEquals(
            mapOf("building:use" to "office", "check_date" to nowAsCheckDateString()),
            OFFICE.buildingUseAppliedTo(mapOf("building:use" to "office"))
        )
    }

    @Test fun `cannot apply non building as new building use`() {
        assertFails {
            SILO.buildingUseAppliedTo(mapOf())
        }
    }
}

private fun BuildingType.appliedTo(tags: Map<String, String>): Map<String, String> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}

private fun BuildingType.buildingUseAppliedTo(
    tags: Map<String, String>
): Map<String, String> {
    val cb = StringMapChangesBuilder(tags)
    applyBuildingUseTo(cb)

    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}
