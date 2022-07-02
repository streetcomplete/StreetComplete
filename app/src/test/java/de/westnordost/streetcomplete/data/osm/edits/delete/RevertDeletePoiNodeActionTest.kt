package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.copy
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RevertDeletePoiNodeActionTest {

    private val e = node(1, tags = mutableMapOf("amenity" to "atm"), version = 2)

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test fun `restore deleted element`() {
        assertEquals(
            e.copy(
                version = 3,
                timestampEdited = 0
            ),
            RevertDeletePoiNodeAction.createUpdates(e, null, repos, provider).modifications
                .single()
                .copy(timestampEdited = 0)
        )
    }

    @Test fun `restore element with cleared tags`() {
        assertEquals(
            e.copy(
                version = 3,
                timestampEdited = 0
            ),
            RevertDeletePoiNodeAction.createUpdates(e, e.copy(version = 3), repos, provider).modifications
                .single()
                .copy(timestampEdited = 0)
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if there is already a newer version`() {
        // version 3 would be the deletion
        RevertDeletePoiNodeAction.createUpdates(e, e.copy(version = 4), repos, provider)
    }
}
