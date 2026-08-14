package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ChangesetTooLargeException
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.testutils.edit
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ElementEditUploaderTest {

    private lateinit var changesetManager: OpenChangesetsManager
    private lateinit var mapDataApi: MapDataApiClient
    private lateinit var mapDataSource: MapDataSource
    private lateinit var uploader: ElementEditUploader

    @BeforeTest fun setUp() {
        changesetManager = mock()
        mapDataApi = mock()
        mapDataSource = mock()

        uploader = ElementEditUploader(changesetManager, mapDataApi, mapDataSource)
    }

    @Test fun `create new changeset when changeset is too large`(): Unit = runBlocking {
        val action = mock<ElementEditAction>()
        val edit = edit(action = action)
        every { action.createUpdates(any(), any()) } returns MapDataChanges()

        // current changeset is 1
        everySuspend { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) } returns 1L
        // but when uploading using this changeset, exception is thrown
        everySuspend { mapDataApi.uploadChanges(1L, any(), any()) } throws ChangesetTooLargeException()

        // creating a changeset yields id 2
        everySuspend { changesetManager.createChangeset(any(), any(), any()) } returns 2
        // and uploading changes to this changeset yields some result
        val mapDataUpdates = MapDataUpdates()
        everySuspend { mapDataApi.uploadChanges(2L, any(), any()) } returns mapDataUpdates

        assertEquals(
            mapDataUpdates,
            uploader.upload(edit, { ElementIdProvider(emptyList()) })
        )
    }

    @Test fun `passes on conflict exception`(): Unit = runBlocking {
        val action = mock<ElementEditAction>()
        val edit = edit(action = action)
        every { action.createUpdates(any(), any()) } returns MapDataChanges()

        everySuspend { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) } returns 1
        everySuspend { changesetManager.createChangeset(any(), any(), any()) } returns 1
        everySuspend { mapDataApi.uploadChanges(any(), any(), any()) } throws ConflictException()

        assertFailsWith<ConflictException> {
            uploader.upload(edit, { ElementIdProvider(emptyList()) })
        }
    }

    @Test fun `handles changeset conflict exception`(): Unit = runBlocking {
        val action = mock<ElementEditAction>()
        val edit = edit(action = action)
        every { action.createUpdates(any(), any()) } returns MapDataChanges()

        everySuspend { changesetManager.getOrCreateChangeset(any(), any(), any(), any()) } returns 1
        everySuspend { changesetManager.createChangeset(any(), any(), any()) } returns 1
        everySuspend { mapDataApi.uploadChanges(any(), any(), any()) } sequentially {
            throws(ConflictException())
            returns(MapDataUpdates())
        }
        every { mapDataSource.getNodes(emptySet()) } returns emptyList()
        uploader.upload(edit, { ElementIdProvider(emptyList()) })
    }
}
