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
            mapOf("building" to "roof"),
            ROOF.appliedTo(mapOf(
                "building" to "residential",
                "ruins" to "yes",
                "abandoned" to "yes",
                "disused" to "yes",
                "historic" to "yes",
            ))
        )

        assertEquals(
            mapOf("building" to "residential", "historic" to "yes"),
            HISTORIC.appliedTo(mapOf(
                "building" to "residential",
                "ruins" to "yes",
                "abandoned" to "yes",
                "disused" to "yes",
            ))
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo(mapOf("building" to "residential", "ruins" to "yes"))
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
            mapOf("building" to "hut", "historic" to "yes", "check_date" to nowAsCheckDateString()),
            HISTORIC.appliedTo(mapOf("building" to "hut", "historic" to "yes"))
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
}

private fun BuildingType.appliedTo(tags: Map<String, String>): Map<String, String> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}
