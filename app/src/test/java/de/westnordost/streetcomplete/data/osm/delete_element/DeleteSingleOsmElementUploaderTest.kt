package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_NOT_FOUND

class DeleteSingleOsmElementUploaderTest {
    private val mapDataApi: MapDataApi = mock()
    private val uploader = DeleteSingleOsmElementUploader(mapDataApi)

    private val e = OsmNode(1L, 1, 0.0, 0.0, null)

    @Before fun setUp() {
        reset(mapDataApi)
    }

    @Test fun `upload deleted`() {
        uploader.upload(100L, e)
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(mapDataApi).uploadChanges(eq(100L), arg.capture(), any())
        assertEquals(e, arg.value.single())
    }

    @Test(expected = ElementConflictException::class)
    fun `pass osm conflict`() {
        on(mapDataApi.uploadChanges(any(), any(), any())).thenThrow(OsmConflictException(HTTP_CONFLICT, "Conflict", "Invalid element version"))
        uploader.upload(100L, e)
    }

    @Test(expected = ElementConflictException::class)
    fun `pass deleted conflict`() {
        on(mapDataApi.uploadChanges(any(), any(), any())).thenThrow(OsmNotFoundException(HTTP_NOT_FOUND, "Not found", "Something"))
        uploader.upload(100L, e)
    }
}
