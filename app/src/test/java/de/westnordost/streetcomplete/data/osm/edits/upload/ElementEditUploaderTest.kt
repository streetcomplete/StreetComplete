package de.westnordost.streetcomplete.data.osm.edits.upload

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
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify

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
    fun `throws deleted exception if node is no more`() {
        on(mapDataApi.getNode(12)).thenReturn(null)
        on(mapDataController.getNode(12)).thenReturn(node(12))
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())
        uploader.upload(edit(element = node(12)), { mock() })
    }

    @Test(expected = ConflictException::class)
    fun `throws deleted exception if way is no more`() {
        on(mapDataApi.getWay(12)).thenReturn(null)
        on(mapDataController.getWay(12)).thenReturn(way(12))
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())
        uploader.upload(edit(element = way(12)), { mock() })
    }

    @Test(expected = ConflictException::class)
    fun `throws deleted exception if relation is no more`() {
        on(mapDataApi.getRelation(12)).thenReturn(null)
        on(mapDataApi.getRelation(12)).thenReturn(rel(12))
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException())
        uploader.upload(edit(element = rel(12)), { mock() })
    }

    @Test
    fun `doesn't download element if no exception`() {
        on(mapDataApi.getNode(12)).thenThrow(NullPointerException()) // ConflictException is handled!
        on(mapDataController.getNode(12)).thenReturn(node(12))
        uploader.upload(edit(element = node(12)), { mock() })
    }

    @Test
    fun `downloads element on exception`() {
        on(mapDataApi.getNode(12)).thenReturn(node(12))
        on(mapDataController.getNode(12)).thenReturn(node(12))
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(ConflictException()).thenReturn(null)
        uploader.upload(edit(element = node(12)), { mock() })
        verify(mapDataController).getNode(12)
        verify(mapDataApi).getNode(12)
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
