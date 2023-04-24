package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Test

class RevertUpdateElementTagsActionTest {

    // TODO other tests

    @Test fun idsUpdatesApplied() {
        val way = way(id = -1)
        val action = RevertUpdateElementTagsAction(way, StringMapChanges(listOf()))
        val idUpdates = mapOf(ElementKey(ElementType.WAY, -1) to 5L)

        assertEquals(
            RevertUpdateElementTagsAction(way.copy(id = 5), StringMapChanges(listOf())),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.WAY, -1)),
            RevertUpdateElementTagsAction(way(id = -1), StringMapChanges(listOf())).elementKeys
        )
    }
}
