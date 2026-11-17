package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class BuildingTypeAndStatusCreatorKtTest {

    @Test fun `set to unsupported does not change anything`() {
        // except for the check date, which is set when nothing is changed :-)

        assertEquals(
            mapOf("check_date" to nowAsCheckDateString()),
            BuildingTypeAndStatus(UNSUPPORTED, null).appliedTo()
        )
        assertEquals(
            mapOf("building" to "blurb", "check_date" to nowAsCheckDateString()),
            BuildingTypeAndStatus(UNSUPPORTED, null).appliedTo("building" to "blurb")
        )

        // additional building-related tags are left as-is
        assertEquals(
            mapOf(
                "building" to "house",
                "house" to "hobbit_house",
                "check_date" to nowAsCheckDateString()
            ),
            BuildingTypeAndStatus(UNSUPPORTED, null).appliedTo(
                "building" to "house",
                "house" to "hobbit_house"
            )
        )

        // so in this case, as status = null, the construction info is put back into the
        // building tag, but without any additional changes
        assertEquals(
            mapOf("building" to "blurb"),
            BuildingTypeAndStatus(UNSUPPORTED, null).appliedTo("construction" to "blurb")
        )
    }

    @Test fun `set to generic building`() {
        assertEquals(
            mapOf("building" to "yes"),
            BuildingTypeAndStatus(null, null).appliedTo()
        )
        assertEquals(
            mapOf("building" to "yes"),
            BuildingTypeAndStatus(null, null).appliedTo("building" to "residential")
        )
        assertEquals(
            mapOf("building" to "yes"),
            BuildingTypeAndStatus(null, null).appliedTo("construction" to "residential")
        )
    }

    @Test fun `set to generic building removes additional building tags`() {
        assertEquals(
            mapOf("building" to "yes"),
            BuildingTypeAndStatus(null, null).appliedTo(
                "building" to "house",
                "house" to "detached",
                "man_made" to "tower",
            )
        )
    }

    @Test fun `set building`() {
        assertEquals(
            mapOf("building" to "residential"),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo()
        )

        assertEquals(
            mapOf("building" to "residential"),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo("building" to "yes")
        )

        assertEquals(
            mapOf("building" to "residential"),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo("man_made" to "storage_tank")
        )
    }

    @Test fun `set building removes additional building tags`() {
        // ah yes, the storage tank re-fitted to be a residential building ;-) …
        // no actually, they do exist: https://en.wikipedia.org/wiki/Vienna_Gasometers
        assertEquals(
            mapOf("building" to "residential"),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo(
                "man_made" to "storage_tank",
                "house" to "something"
            )
        )
    }

    @Test fun `set man-made`() {
        assertEquals(
            mapOf("man_made" to "storage_tank"),
            BuildingTypeAndStatus(STORAGE_TANK, null).appliedTo()
        )

        assertEquals(
            mapOf("man_made" to "storage_tank"),
            BuildingTypeAndStatus(STORAGE_TANK, null).appliedTo("man_made" to "silo")
        )


    }

    @Test fun `set man made removes additional building tags`() {
        assertEquals(
            mapOf("man_made" to "storage_tank"),
            BuildingTypeAndStatus(STORAGE_TANK, null).appliedTo(
                "building" to "house",
                "house" to "detached"
            )
        )
    }

    @Test fun `don't overwrite aliases`() {
        assertEquals(
            mapOf("building" to "livestock", "check_date" to nowAsCheckDateString()),
            BuildingTypeAndStatus(FARM_AUXILIARY, null).appliedTo("building" to "livestock")
        )

        assertEquals(
            mapOf("building" to "residential"),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo("building" to "livestock")
        )
    }

    @Test fun `don't overwrite house aliases`() {
        assertEquals(
            mapOf("building" to "detached"),
            BuildingTypeAndStatus(DETACHED, null).appliedTo()
        )

        assertEquals(
            mapOf(
                "building" to "house",
                "house" to "semi-detached",
                "check_date" to nowAsCheckDateString()
            ),
            BuildingTypeAndStatus(SEMI_DETACHED, null).appliedTo(
                "building" to "house",
                "house" to "semi-detached"
            )
        )

        // but only preserve previous tag, don't adhere to that schema when re-tagging
        assertEquals(
            mapOf("building" to "semidetached_house"),
            BuildingTypeAndStatus(SEMI_DETACHED, null).appliedTo(
                "building" to "house",
                "house" to "detached"
            )
        )
    }

    @Test fun `set terraced house`() {
        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            BuildingTypeAndStatus(TERRACED_HOUSE, null).appliedTo()
        )

        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            BuildingTypeAndStatus(TERRACED_HOUSE, null).appliedTo(
                "building" to "house",
                "house" to "detached"
            )
        )

        assertEquals(
            mapOf("building" to "house", "house" to "terraced"),
            BuildingTypeAndStatus(TERRACED_HOUSE, null).appliedTo("building" to "detached")
        )
    }

    @Test fun `sets lifecycle`() {
        assertEquals(
            mapOf("disused" to "yes"),
            BuildingTypeAndStatus(UNSUPPORTED, DISUSED).appliedTo()
        )
        assertEquals(
            mapOf("abandoned" to "yes"),
            BuildingTypeAndStatus(UNSUPPORTED, ABANDONED).appliedTo()
        )
        assertEquals(
            mapOf("ruins" to "yes"),
            BuildingTypeAndStatus(UNSUPPORTED, RUINS).appliedTo()
        )

        // construction only for not-buildings because construction-tagging for buildings is special
        // see `tagging construction on buildings`
        assertEquals(
            mapOf("construction" to "yes", "man_made" to "tower"),
            BuildingTypeAndStatus(TOWER, CONSTRUCTION).appliedTo()
        )
    }

    @Test fun `clears previous lifecycle tags and building use on setting lifecycle`() {
        assertEquals(
            mapOf("disused" to "yes"),
            BuildingTypeAndStatus(UNSUPPORTED, DISUSED).appliedTo(
                "disused" to "yes",
                "abandoned" to "yes",
                "ruins" to "maybe",
                "construction:building" to "house",
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
            BuildingTypeAndStatus(UNSUPPORTED, APARTMENTS).appliedTo()
        )
    }

    @Test fun `clears previous lifecycle tags and building use on setting use`() {
        assertEquals(
            mapOf("building:use" to "detached"),
            BuildingTypeAndStatus(UNSUPPORTED, DETACHED).appliedTo(
                "disused" to "yes",
                "abandoned" to "yes",
                "ruins" to "maybe",
                "construction:building" to "house",
                "abandoned:building" to "house",
                "disused:building" to "house",
                "ruins:building" to "house",
                "building:use" to "apartments",
                "man_made:use" to "tower",
            )
        )
    }

    @Test fun `udpate check date when nothing was changed`() {
        assertEquals(
            mapOf("building" to "residential", "check_date" to nowAsCheckDateString()),
            BuildingTypeAndStatus(RESIDENTIAL, null).appliedTo("building" to "residential")
        )

        assertEquals(
            mapOf("abandoned" to "yes", "check_date" to nowAsCheckDateString()),
            BuildingTypeAndStatus(UNSUPPORTED, ABANDONED).appliedTo("abandoned" to "yes")
        )
    }

    @Test fun `tagging construction on buildings`() {
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
            BuildingTypeAndStatus(UNSUPPORTED, ABANDONED).appliedTo()
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
            mapOf("building" to "yes"),
            BuildingTypeAndStatus(null, null).appliedTo(
                "building" to "construction",
                "construction" to "house"
            )
        )
        assertEquals(
            mapOf("building" to "blurb"),
            BuildingTypeAndStatus(UNSUPPORTED, null).appliedTo(
                "building" to "construction",
                "construction" to "blurb"
            )
        )
    }
}

private fun BuildingTypeAndStatus.appliedTo(vararg tags: Pair<String, String>): Map<String, String> {
    val tags = mapOf(*tags)
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    val mutableMap = tags.toMutableMap()
    cb.create().applyTo(mutableMap)
    return mutableMap
}

