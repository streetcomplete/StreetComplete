package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doThrow

class ElementEditUploaderTest {

    private lateinit var changesetManager: OpenChangesetsManager
    private lateinit var mapDataApi: MapDataApi
    private lateinit var mapDataController: MapDataController
    private lateinit var uploader: ElementEditUploader

    @Before fun setUp() {
        changesetManager = mock()
        mapDataApi = mock()
        mapDataController = mock()

        uploader = ElementEditUploader(changesetManager, mapDataApi, mapDataController)
    }

    @Test(expected = ConflictException::class)
    fun `passes on conflict exception`() {
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())
        uploader.upload(edit, { mock() })
    }

    @Test(expected = ConflictException::class)
    fun `passes on element conflict exception`() {
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())

        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        on(mapDataApi.uploadChanges(anyLong(), any(), any()))
            .thenThrow(ConflictException())
            .thenThrow(ConflictException())

        uploader.upload(edit, { mock() })
    }

    @Test fun `handles changeset conflict exception`() {
        val edit: ElementEdit = mock()
        val action: ElementEditAction = mock()
        on(edit.action).thenReturn(action)
        on(action.createUpdates(any(), any())).thenReturn(MapDataChanges())

        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        doThrow(ConflictException()).doAnswer { MapDataUpdates() }
            .on(mapDataApi).uploadChanges(anyLong(), any(), any())

        uploader.upload(edit, { mock() })
    }
}
