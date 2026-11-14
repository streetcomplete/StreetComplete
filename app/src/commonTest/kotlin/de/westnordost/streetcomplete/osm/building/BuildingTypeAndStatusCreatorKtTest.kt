package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class BuildingTypeAndStatusCreatorKtTest {

    @Test fun `set building as unsupported not possible`() {
        assertFails { UNSUPPORTED.appliedTo() }
    }

    @Test fun `set building`() {
        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo()
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo("building" to "yes")
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo("man_made" to "storage_tank")
        )
    }

    @Test fun `set man-made`() {
        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo()
        )

        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo("man_made" to "silo")
        )

        assertEquals(
            mapOf("man_made" to "storage_tank"),
            STORAGE_TANK.appliedTo("building" to "roof")
        )
    }

    @Test fun `don't overwrite aliases`() {
        assertEquals(
            mapOf("building" to "livestock", "check_date" to nowAsCheckDateString()),
            FARM_AUXILIARY.appliedTo("building" to "livestock")
        )

        assertEquals(
            mapOf("building" to "residential"),
            RESIDENTIAL.appliedTo("building" to "livestock")
        )
    }

    @Test fun `preserve house subtag schema`() {
        assertEquals(
            mapOf("building" to "detached"),
            DETACHED.appliedTo()
        )

        assertEquals(
            mapOf("building" to "house", "house" to "semi-detached"),
            SEMI_DETACHED.appliedTo("building" to "house", "house" to "semi-detached")
        )

        // but only preserve previous tag, don't adhere to that schema when re-tagging
        assertEquals(
            mapOf("building" to "semidetached_house"),
            SEMI_DETACHED.appliedTo("building" to "house", "house" to "semi-detached")
        )
    }

    @Test fun `set terraced house`() {
        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            TERRACED_HOUSE.appliedTo()
        )

        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            TERRACED_HOUSE.appliedTo("building" to "house", "house" to "detached")
        )

        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            TERRACED_HOUSE.appliedTo("building" to "detached")
        )
    }

    @Test fun `removes house subtag when retagging as not a house`() {
        assertEquals(
            mapOf("building" to "apartments"),
            APARTMENTS.appliedTo("building" to "house", "house" to "detached")
        )
    }


    @Test fun `sets lifecycle`() {
        assertEquals(mapOf("disused" to "yes"), DISUSED.appliedTo())
        assertEquals(mapOf("abandoned" to "yes"), ABANDONED.appliedTo())
        assertEquals(mapOf("ruins" to "yes"), RUINS.appliedTo())
    }

    @Test fun `clears previous lifecycle tags and building use on setting lifecycle`() {
        assertEquals(
            mapOf("disused" to "yes"),
            DISUSED.appliedTo(
                "disused" to "yes",
                "abandoned" to "yes",
                "ruins" to "maybe",
                "construction" to "house",
                "abandoned:building" to "house",
                "disused:building" to "house",
                "ruins:building" to "house",
                "building:use" to "apartments",
                "man_made:use" to "tower",
            )
        )
    }

    @Test fun `sets use`() {
        assertEquals(
            mapOf("building:use" to "apartments"),
            BuildingTypeAndStatus(null, APARTMENTS).appliedTo()
        )
        assertEquals(
            mapOf("man_made:use" to "tower"),
            BuildingTypeAndStatus(null, TOWER).appliedTo()
        )
    }

    @Test fun `clears previous lifecycle tags and building use on setting use`() {
        assertEquals(
            mapOf("building:use" to "detached"),
            BuildingTypeAndStatus(null, DETACHED).appliedTo(
                "disused" to "yes",
                "abandoned" to "yes",
                "ruins" to "maybe",
                "construction" to "house",
                "abandoned:building" to "house",
                "disused:building" to "house",
                "ruins:building" to "house",
                "building:use" to "apartments",
                "man_made:use" to "tower",
            )
        )
    }

    @Test fun `udpate check date`() {
        assertEquals(
            mapOf("building" to "residential", "check_date" to nowAsCheckDateString()),
            RESIDENTIAL.appliedTo("building" to "residential")
        )

        assertEquals(
            mapOf("abandoned" to "yes", "check_date" to nowAsCheckDateString()),
            ABANDONED.appliedTo("abandoned" to "yes")
        )
    }

    @Test fun `tagging construction`() {
        assertEquals(
            mapOf("building" to "construction"),
            BuildingTypeAndStatus(null, CONSTRUCTION).appliedTo()
        )

        assertEquals(
            mapOf("building" to "construction"),
            BuildingTypeAndStatus(null, CONSTRUCTION).appliedTo("construction" to "house")
        )

        assertEquals(
            mapOf("building" to "construction", "construction" to "house"),
            BuildingTypeAndStatus(HOUSE, CONSTRUCTION).appliedTo()
        )

        assertEquals(
            mapOf("building" to "construction", "construction" to "house", "house" to "terraced"),
            BuildingTypeAndStatus(TERRACED_HOUSE, CONSTRUCTION).appliedTo()
        )
    }

    @Test fun `tagging construction clears previous building status`() {
        assertEquals(
            mapOf("building" to "construction"),
            BuildingTypeAndStatus(null, CONSTRUCTION).appliedTo(
                "disused" to "yes",
                "abandoned" to "yes",
                "ruins" to "maybe",
                "building" to "house",
                "construction" to "house",
                "abandoned:building" to "house",
                "disused:building" to "house",
                "ruins:building" to "house",
                "building:use" to "apartments",
                "man_made:use" to "tower",
            )
        )
    }

    @Test fun `tagging type and use`() {
        assertEquals(
            mapOf("building" to "retail"),
            BuildingTypeAndStatus(RETAIL, null).appliedTo()
        )
        assertEquals(
            mapOf("abandoned" to "yes"),
            BuildingTypeAndStatus(null, ABANDONED).appliedTo()
        )
        assertEquals(
            mapOf("building" to "retail", "abandoned" to "yes"),
            BuildingTypeAndStatus(RETAIL, ABANDONED).appliedTo()
        )
    }


    @Test fun `clears construction tags when removing construction`() {
        assertEquals(
            mapOf("building" to "house"),
            BuildingTypeAndStatus(HOUSE, null).appliedTo(
                "building" to "construction",
                "construction" to "house"
            )
        )
        assertEquals(
            mapOf(),
            BuildingTypeAndStatus(null, null).appliedTo(
                "building" to "construction",
                "construction" to "house"
            )
        )
    }
}

private fun BuildingLifecycle.appliedTo(vararg tags: Pair<String, String>): Map<String, String> =
    BuildingTypeAndStatus(null, this).appliedTo(*tags)

private fun BuildingType.appliedTo(vararg tags: Pair<String, String>): Map<String, String> =
    BuildingTypeAndStatus(this, null).appliedTo(*tags)

private fun BuildingTypeAndStatus.appliedTo(vararg tags: Pair<String, String>): Map<String, String> {
    val tags = mapOf(*tags)
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}

