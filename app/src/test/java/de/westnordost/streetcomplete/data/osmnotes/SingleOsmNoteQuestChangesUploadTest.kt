package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.ImageUploader
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

class SingleOsmNoteQuestChangesUploadTest {
    private lateinit var osmDao: NotesDao
    private lateinit var imageUploader: ImageUploader
    private lateinit var noteUploader: SingleOsmNoteQuestChangesUpload

    @Before fun setUp() {
        osmDao = mock(NotesDao::class.java)
        on(osmDao.comment(anyLong(), any())).thenReturn(createNote())
        imageUploader = mock(ImageUploader::class.java)
        noteUploader = SingleOsmNoteQuestChangesUpload(osmDao, imageUploader)
    }

    @Test fun `uploads comment with no pictures`() {
        noteUploader.upload(createQuest())

        verify(osmDao).comment(1, "blablub")
        verify(imageUploader, never()).upload(anyList())
        verify(imageUploader, never()).activate(anyLong())
    }

    @Test fun `uploads comment with pictures`() {
        val quest = createQuest()
        quest.imagePaths = listOf("Never say")

        val returnedNote = Note()
        returnedNote.id = 123
        val returnedImagePaths = listOf("never")

        on(imageUploader.upload(quest.imagePaths)).thenReturn(returnedImagePaths)
        on(osmDao.comment(anyLong(), anyString())).thenReturn(returnedNote)

        noteUploader.upload(quest)

        verify(osmDao).comment(1, "blablub\n\nAttached photo(s):\nnever")
        verify(imageUploader).activate(returnedNote.id)
    }

    @Test(expected = ConflictException::class)
    fun `not found exception is rethrown as ConflictException`() {
        on(osmDao.comment(anyLong(), any())).thenThrow(OsmNotFoundException(404, "title", "desc"))
        noteUploader.upload(createQuest())
    }

    @Test(expected = ConflictException::class)
    fun `conflict exception is rethrown as ConflictException`() {
        on(osmDao.comment(anyLong(), any())).thenThrow(OsmConflictException(409, "title", "desc"))
        noteUploader.upload(createQuest())
    }

    private fun createNote(): Note {
        val note = Note()
        note.id = 1
        note.position = OsmLatLon(1.0, 2.0)
        return note
    }

    private fun createQuest(): OsmNoteQuest {
        val quest = OsmNoteQuest(createNote(), OsmNoteQuestType())
        quest.id = 3
        quest.comment = "blablub"
        return quest
    }
}
