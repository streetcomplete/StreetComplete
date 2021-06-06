package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doThrow

class ElementEditUploaderTest {

    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var mapDataApi: MapDataApi
    private lateinit var uploader: ElementEditUploader

    @Before fun setUp() {
        changesetManager = mock()
        mapDataApi = mock()

        uploader = ElementEditUploader(changesetManager, mapDataApi)
    }

    @Test(expected = ConflictException::class)
    fun `throws deleted exception if node is no more`() {
        on(mapDataApi.getNode(12)).thenReturn(null)
        uploader.upload(edit(element = node(12)), mock())
    }

    @Test(expected = ConflictException::class)
    fun `throws deleted exception if way is no more`() {
        on(mapDataApi.getWay(12)).thenReturn(null)
        uploader.upload(edit(element = way(12)), mock())
    }

    @Test(expected = ConflictException::class)
    fun `throws deleted exception if relation is no more`() {
        on(mapDataApi.getRelation(12)).thenReturn(null)
        uploader.upload(edit(element = rel(12)), mock())
    }

    @Test(expected = ConflictException::class)
    fun `passes on element conflict exception`() {
        val node = node(1)
        on(mapDataApi.getNode(anyLong())).thenReturn(node)
        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        on(mapDataApi.uploadChanges(anyLong(), any()))
            .thenThrow(ConflictException())
            .thenThrow(ConflictException())

        uploader.upload(edit(element = node(1)), mock())
    }

    @Test fun `handles changeset conflict exception`() {
        val node = node(1)
        on(mapDataApi.getNode(anyLong())).thenReturn(node)
        on(changesetManager.getOrCreateChangeset(any(), any())).thenReturn(1)
        on(changesetManager.createChangeset(any(), any())).thenReturn(1)
        doThrow(ConflictException()).doAnswer { MapDataUpdates() }
            .on(mapDataApi).uploadChanges(anyLong(), any())

        uploader.upload(edit(element = node(1)), mock())
    }
}
