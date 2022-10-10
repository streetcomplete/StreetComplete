package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doThrow

class ElementEditUploaderTest {

    private lateinit var changesetManager: OpenChangesetsManager
    private lateinit var mapDataApi: MapDataApi
    private lateinit var mapDataController: MapDataController
    private lateinit var createdElementsSource: CreatedElementsSource
    private lateinit var uploader: ElementEditUploader

    @Before fun setUp() {
        changesetManager = mock()
        mapDataApi = mock()
        mapDataController = mock()
        createdElementsSource = mock()

        uploader = ElementEditUploader(changesetManager, mapDataApi, mapDataController, createdElementsSource)
    }

    @Test(expected = ConflictException::class)
    fun `passes on conflict exception`() {
        val node = node(1)
        on(mapDataApi.getNode(anyLong())).thenReturn(node)
        on(mapDataController.getNode(anyLong())).thenReturn(node)
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())
        uploader.upload(edit(element = node(1)), { mock() })
    }

    @Test(expected = ConflictException::class)
    fun `passes on element conflict exception`() {
        val node = node(1)
        on(mapDataApi.getNode(anyLong())).thenReturn(node)
        on(mapDataController.getNode(anyLong())).thenReturn(node)
        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        on(mapDataApi.uploadChanges(anyLong(), any(), any()))
            .thenThrow(ConflictException())
            .thenThrow(ConflictException())

        uploader.upload(edit(element = node(1)), { mock() })
    }

    @Test fun `handles changeset conflict exception`() {
        val node = node(1)
        on(mapDataApi.getNode(anyLong())).thenReturn(node)
        on(mapDataController.getNode(anyLong())).thenReturn(node)
        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        doThrow(ConflictException()).doAnswer { MapDataUpdates() }
            .on(mapDataApi).uploadChanges(anyLong(), any(), any())

        uploader.upload(edit(element = node(1)), { mock() })
    }
}
