package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ChangesetTooLargeException
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.testutils.edit
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.sequentiallyRepeat
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ElementEditUploaderTest {

    private lateinit var changesetManager: OpenChangesetsManager
    private lateinit var mapDataApi: MapDataApiClient
    private lateinit var mapDataController: MapDataController
    private lateinit var uploader: ElementEditUploader

    @BeforeTest fun setUp() {
        changesetManager = mock()
        mapDataApi = mock()
        mapDataController = mock()

        uploader = ElementEditUploader(changesetManager, mapDataApi, mapDataController)
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
            uploader.upload(edit, { mock() })
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
            uploader.upload(edit, { mock() })
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
        uploader.upload(edit, { mock() })
    }
}
