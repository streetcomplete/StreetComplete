package de.westnordost.streetcomplete.overlays.address

import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddressOverlayFormTest {

    @Test fun `removes address tags from building way without adding noaddress`() {
        val building = way(nodes = listOf(1, 2, 3, 1), tags = mapOf(
            "building" to "house",
            "name" to "Town Hall",
            "addr:housenumber" to "12",
            "addr:street" to "Main Street",
            "source:addr:housenumber" to "survey",
            "note:addr:street" to "was renamed",
            "noaddress" to "yes",
            "nohousenumber" to "yes",
        ))

        assertEquals(
            UpdateElementTagsAction(building, StringMapChanges(setOf(
                StringMapEntryDelete("addr:housenumber", "12"),
                StringMapEntryDelete("addr:street", "Main Street"),
                StringMapEntryDelete("source:addr:housenumber", "survey"),
                StringMapEntryDelete("note:addr:street", "was renamed"),
                StringMapEntryDelete("noaddress", "yes"),
                StringMapEntryDelete("nohousenumber", "yes"),
            ))),
            createRemoveAddressElementEditAction(building)
        )
    }

    @Test fun `removes address tags from building multipolygon without adding noaddress`() {
        val building = rel(tags = mapOf(
            "type" to "multipolygon",
            "building" to "apartments",
            "name" to "Block A",
            "addr:housenumber" to "5",
            "addr:street" to "Ring Road",
        ))

        assertEquals(
            UpdateElementTagsAction(building, StringMapChanges(setOf(
                StringMapEntryDelete("addr:housenumber", "5"),
                StringMapEntryDelete("addr:street", "Ring Road"),
            ))),
            createRemoveAddressElementEditAction(building)
        )
    }

    @Test fun `deletes address-only node`() {
        val addressNode = node(tags = mapOf(
            "addr:housenumber" to "4",
            "addr:street" to "Oak Road",
        ))

        assertEquals(
            DeletePoiNodeAction(addressNode),
            createRemoveAddressElementEditAction(addressNode)
        )
    }

    @Test fun `removes address tags from node with entrance or poi tags`() {
        val entrance = node(tags = mapOf(
            "entrance" to "yes",
            "addr:housenumber" to "8",
            "addr:street" to "Station Road",
            "level" to "0",
        ))
        assertEquals(
            UpdateElementTagsAction(entrance, StringMapChanges(setOf(
                StringMapEntryDelete("addr:housenumber", "8"),
                StringMapEntryDelete("addr:street", "Station Road"),
            ))),
            createRemoveAddressElementEditAction(entrance)
        )

        val poi = node(id = 2, tags = mapOf(
            "shop" to "bakery",
            "name" to "Bun Shop",
            "addr:housenumber" to "3",
            "addr:street" to "Mill Lane",
        ))
        assertEquals(
            UpdateElementTagsAction(poi, StringMapChanges(setOf(
                StringMapEntryDelete("addr:housenumber", "3"),
                StringMapEntryDelete("addr:street", "Mill Lane"),
            ))),
            createRemoveAddressElementEditAction(poi)
        )
    }

    @Test fun `returns null when building has no removable address tags`() {
        val building = way(nodes = listOf(1, 2, 3, 1), tags = mapOf(
            "building" to "yes",
            "name" to "Shed",
        ))

        assertNull(createRemoveAddressElementEditAction(building))
    }
}
