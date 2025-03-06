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
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doThrow
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
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())

        // current changeset is 1
        on(changesetManager.getOrCreateChangeset(any(), any(), any(), anyBoolean())).thenReturn(1L)
        // but when uploading using this changeset, exception is thrown
        on(mapDataApi.uploadChanges(eq(1L), any(), any())).thenThrow(ChangesetTooLargeException())

        // creating a changeset yields id 2
        on(changesetManager.createChangeset(any(), any(), any())).thenReturn(2)
        // and uploading changes to this changeset yields some result
        val mapDataUpdates = MapDataUpdates()
        on(mapDataApi.uploadChanges(eq(2L), any(), any())).thenReturn(mapDataUpdates)

        assertEquals(
            mapDataUpdates,
            uploader.upload(edit, { mock() })
        )
    }

    @Test fun `passes on conflict exception`(): Unit = runBlocking {
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())

        on(changesetManager.getOrCreateChangeset(any(), any(), any(), anyBoolean())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any(), any())).thenReturn(1)
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())

        assertFailsWith<ConflictException> {
            uploader.upload(edit, { mock() })
        }
    }

    @Test fun `handles changeset conflict exception`(): Unit = runBlocking {
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())

        on(changesetManager.getOrCreateChangeset(any(), any(), any(), anyBoolean())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any(), any())).thenReturn(1)
        doThrow(ConflictException()).doAnswer { MapDataUpdates() }
            .on(mapDataApi).uploadChanges(anyLong(), any(), any())

        uploader.upload(edit, { mock() })
    }
}
