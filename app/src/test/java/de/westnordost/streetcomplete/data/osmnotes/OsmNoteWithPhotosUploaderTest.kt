package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class OsmNoteWithPhotosUploaderTest {
    private lateinit var notesApi: NotesApi
    private lateinit var imageUploader: StreetCompleteImageUploader
    private lateinit var uploader: OsmNoteWithPhotosUploader

    @Before fun setUp() {
        notesApi = mock()
        on(notesApi.comment(anyLong(), any())).thenReturn(createNote())
        on(notesApi.create(any(), any())).thenReturn(createNote())
        imageUploader = mock()
        uploader = OsmNoteWithPhotosUploader(notesApi, imageUploader)
    }

    @Test fun `uploads note with no pictures`() {
        val pos = OsmLatLon(1.0, 2.0)
        uploader.create(pos, "blablub", null)

        verify(notesApi).create(pos, "blablub")
        verify(imageUploader, never()).upload(anyList())
        verify(imageUploader, never()).activate(anyLong())
    }

    @Test fun `uploads comment with no pictures`() {
        uploader.comment(1, "blablub", null)

        verify(notesApi).comment(1, "blablub")
        verify(imageUploader, never()).upload(anyList())
        verify(imageUploader, never()).activate(anyLong())
    }

    @Test fun `uploads comment with pictures`() {
        val imagePaths = listOf("Never say")

        val returnedNote = Note()
        returnedNote.id = 123
        val returnedImagePaths = listOf("never")

        on(imageUploader.upload(imagePaths)).thenReturn(returnedImagePaths)
        on(notesApi.comment(anyLong(), anyString())).thenReturn(returnedNote)

        uploader.comment(1, "blablub", imagePaths)

        verify(notesApi).comment(1, "blablub\n\nAttached photo(s):\nnever")
        verify(imageUploader).activate(returnedNote.id)
    }

    @Test fun `uploads note with pictures`() {
        val pos = OsmLatLon(2.0,1.0)
        val imagePaths = listOf("Never say")

        val returnedNote = Note()
        returnedNote.id = 123
        val returnedImagePaths = listOf("never")

        on(imageUploader.upload(imagePaths)).thenReturn(returnedImagePaths)
        on(notesApi.create(any(), anyString())).thenReturn(returnedNote)

        uploader.create(pos, "blablub", imagePaths)

        verify(notesApi).create(pos, "blablub\n\nAttached photo(s):\nnever")
        verify(imageUploader).activate(returnedNote.id)
    }

    @Test(expected = ConflictException::class)
    fun `not found exception is rethrown as ConflictException`() {
        on(notesApi.comment(anyLong(), any())).thenThrow(OsmNotFoundException(404, "title", "desc"))
        uploader.comment(1, "bla", null)
    }

    @Test(expected = ConflictException::class)
    fun `conflict exception is rethrown as ConflictException`() {
        on(notesApi.comment(anyLong(), any())).thenThrow(OsmConflictException(409, "title", "desc"))
        uploader.comment(1, "bla", null)
    }

    @Test fun `error on activation of images on commenting is caught`() {
        on(imageUploader.activate(anyLong())).thenThrow(ImageActivationException())
        uploader.comment(1, "bla", listOf("hello"))
    }

    @Test fun `error on activation of images on creating is caught`() {
        on(imageUploader.activate(anyLong())).thenThrow(ImageActivationException())
        uploader.create(OsmLatLon(5.0,6.0), "bla", listOf("hello"))
    }

    @Test(expected = ImageUploadException::class)
    fun `error on upload of images on commenting is not caught`() {
        on(imageUploader.activate(anyLong())).thenThrow(ImageUploadException())
        uploader.comment(1, "bla", listOf("hello"))
    }

    @Test(expected = ImageUploadException::class)
    fun `error on upload of images on creating is not caught`() {
        on(imageUploader.activate(anyLong())).thenThrow(ImageUploadException())
        uploader.create(OsmLatLon(5.0,6.0), "bla", listOf("hello"))
    }

    private fun createNote(): Note {
        val note = Note()
        note.id = 1
        note.position = OsmLatLon(1.0, 2.0)
        return note
    }
}
