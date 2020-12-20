package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.common.errors.OsmApiException
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
import java.net.HttpURLConnection.*

class DeleteSingleOsmElementUploaderTest {
    private val mapDataApi: MapDataApi = mock()
    private val uploader = DeleteSingleOsmElementUploader(mapDataApi)

    private val e = OsmNode(1L, 1, 0.0, 0.0, mutableMapOf("amenity" to "atm"))

    @Before fun setUp() {
        reset(mapDataApi)
    }

    @Test fun `upload deleted`() {
        uploader.upload(100L, e)
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(mapDataApi).uploadChanges(eq(100L), arg.capture(), any())
        assertEquals(e, arg.value.single())
    }


    @Test fun `upload deleted vertex`() {
        doThrow(OsmApiException(HTTP_PRECON_FAILED, "Precondition Failed", "Is part of way XYZ"))
            .doNothing()
            .on(mapDataApi).uploadChanges(anyLong(), any(), any())
        uploader.upload(100L, e)
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(mapDataApi, times(2)).uploadChanges(eq(100L), arg.capture(), any())
        val uploadedNode = (arg.value.single() as Node)
        assertFalse(uploadedNode.isDeleted)
        assertTrue(uploadedNode.tags.isEmpty())
    }

    @Test(expected = ElementConflictException::class)
    fun `pass osm conflict`() {
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(OsmConflictException(HTTP_CONFLICT, "Conflict", "Invalid element version"))
        uploader.upload(100L, e)
    }

    @Test(expected = ElementConflictException::class)
    fun `pass deleted conflict`() {
        on(mapDataApi.uploadChanges(anyLong(), any(), any())).thenThrow(OsmNotFoundException(HTTP_NOT_FOUND, "Not found", "Something"))
        uploader.upload(100L, e)
    }
}
