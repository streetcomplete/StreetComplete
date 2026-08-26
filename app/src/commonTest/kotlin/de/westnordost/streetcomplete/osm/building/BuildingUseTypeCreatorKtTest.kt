package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildingUseTypeCreatorKtTest {

    @Test fun `set building use`() {
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office"
            ),
            OFFICE.appliedToBuildingUse(
                mapOf("building" to "commercial")
            )
        )

        assertEquals(
            mapOf(
                "building" to "residential",
                "building:use" to "apartments"
            ),
            APARTMENTS.appliedToBuildingUse(
                mapOf("building" to "residential")
            )
        )
    }

    @Test fun `replace existing building use`() {
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office"
            ),
            OFFICE.appliedToBuildingUse(
                mapOf(
                    "building" to "commercial",
                    "building:use" to "retail"
                )
            )
        )
    }

    @Test fun `remove redundant building use`() {
        assertEquals(
            mapOf(
                "building" to "office"
            ),
            OFFICE.appliedToBuildingUse(
                mapOf(
                    "building" to "office",
                    "building:use" to "office"
                )
            )
        )
    }

    @Test fun `update check date for building use`() {
        assertEquals(
            mapOf(
                "building" to "commercial",
                "building:use" to "office",
                "check_date:building:use" to nowAsCheckDateString()
            ),
            OFFICE.appliedToBuildingUse(
                mapOf(
                    "building" to "commercial",
                    "building:use" to "office"
                )
            )
        )
    }
}

private fun BuildingType.appliedToBuildingUse(
    tags: Map<String, String>
): Map<String, String> {
    val cb = StringMapChangesBuilder(tags)
    applyToBuildingUse(cb)

    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}
