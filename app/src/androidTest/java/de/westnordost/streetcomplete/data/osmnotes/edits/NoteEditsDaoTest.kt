package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteEditsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NoteEditsDao

    @BeforeTest fun createDao() {
        dao = NoteEditsDao(database)
    }

    @Test fun addGet() {
        val edit = edit(noteId = 1L)
        dao.add(edit)
        assertNotNull(edit.id)
        val dbEdit = dao.get(edit.id)
        assertEquals(edit, dbEdit)
    }

    @Test fun addGetWithAllPropertieS() {
        val edit = NoteEdit(
            1L,
            123L,
            LatLon(1.0, 2.0),
            NoteEditAction.COMMENT,
            "test345",
            listOf("a", "b", "c"),
            4654679L,
            true,
            true,
            listOf(
                Trackpoint(LatLon(3.0, 4.0), 1234L, 1f, 2f),
                Trackpoint(LatLon(1.0, 5.0), 12345L, 2f, 3f),
            )
        )
        dao.add(edit)
        assertEquals(edit, dao.get(1L))
    }

    @Test fun addGetDelete() {
        val edit = edit(noteId = 1L)
        // nothing there
        assertFalse(dao.delete(1L))
        assertNull(dao.get(1L))
        // now it is added
        dao.add(edit)
        assertNotNull(edit.id)
        assertNotNull(dao.get(edit.id))
        // delete again -> nothing there again
        assertTrue(dao.delete(edit.id))
        assertFalse(dao.delete(edit.id))
        assertNull(dao.get(edit.id))
    }

    @Test fun deleteAll() {
        val e1 = edit(1)
        val e2 = edit(2)
        val e3 = edit(3)

        dao.addAll(e1, e2, e3)

        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
        assertNotNull(dao.get(3))

        dao.deleteAll(listOf(1, 2, 3))

        assertNull(dao.get(1))
        assertNull(dao.get(2))
        assertNull(dao.get(3))
    }

    @Test fun getAll() {
        val e1 = edit(timestamp = 100)
        val e2 = edit(timestamp = 10)
        val e3 = edit(timestamp = 1000)

        dao.addAll(e1, e2, e3)

        // sorted by timestamp ascending
        assertEquals(listOf(e2, e1, e3), dao.getAll())
    }

    @Test fun getAllUnsynced() {
        val e1 = edit(timestamp = 10)
        val e2 = edit(timestamp = 100)
        val e3 = edit(timestamp = 1000)
        val e4 = edit(timestamp = 500, isSynced = true)

        dao.addAll(e1, e2, e3, e4)

        // synced are not included, sorted by timestamp ascending
        assertEquals(listOf(e1, e2, e3), dao.getAllUnsynced())
    }

    @Test fun getAllUnsyncedForNote() {
        val e1 = edit(noteId = 1L, timestamp = 10)
        val e2 = edit(noteId = 2L, timestamp = 100)
        val e3 = edit(noteId = 1L, timestamp = 1000)
        val e4 = edit(noteId = 1L, timestamp = 500, isSynced = true)

        dao.addAll(e1, e2, e3, e4)

        // synced are not included, sorted by timestamp ascending
        assertEquals(listOf(e1, e3), dao.getAllUnsyncedForNote(1L))
    }

    @Test fun getAllUnsyncedForNotes() {
        val e1 = edit(noteId = 1L, timestamp = 10)
        val e2 = edit(noteId = 2L, timestamp = 100)
        val e3 = edit(noteId = 1L, timestamp = 1000)
        val e4 = edit(noteId = 1L, timestamp = 500, isSynced = true)
        val e5 = edit(noteId = 4L, timestamp = 2000)

        dao.addAll(e1, e2, e3, e4, e5)

        // synced are not included, sorted by timestamp ascending
        assertEquals(listOf(e1, e3, e5), dao.getAllUnsyncedForNotes(listOf(1L, 3L, 4L)))
    }

    @Test fun getAllUnsyncedForBounds() {
        val posIn1 = LatLon(0.0, 0.0)
        val posIn2 = LatLon(0.5, 0.0)
        val posOut1 = LatLon(-0.5, 0.0)
        val posOut2 = LatLon(1.1, 0.5)
        val posOut3 = LatLon(0.5, 2.5)
        val posOut4 = LatLon(0.5, -0.5)
        val e1 = edit(pos = posOut1)
        val e2 = edit(pos = posOut2)
        val e3 = edit(pos = posOut3)
        val e4 = edit(pos = posOut4)

        val e5 = edit(pos = posIn1, timestamp = 100)
        val e6 = edit(pos = posIn2, timestamp = 1000)

        val e7 = edit(pos = posIn2, timestamp = 500, isSynced = true)

        dao.addAll(e1, e2, e3, e4, e5, e6, e7)

        assertEquals(listOf(e5, e6), dao.getAllUnsynced(BoundingBox(0.0, 0.0, 1.0, 2.0)))
    }

    @Test fun getAllUnsyncedPositionsForBounds() {
        val posIn1 = LatLon(0.0, 0.0)
        val posIn2 = LatLon(0.0, 0.0)
        val posOut1 = LatLon(-0.5, 0.0)
        val posOut2 = LatLon(1.1, 0.5)
        val posOut3 = LatLon(0.5, 2.5)
        val posOut4 = LatLon(0.5, -0.5)
        val e1 = edit(pos = posOut1)
        val e2 = edit(pos = posOut2)
        val e3 = edit(pos = posOut3)
        val e4 = edit(pos = posOut4)

        val e5 = edit(pos = posIn1, timestamp = 100)
        val e6 = edit(pos = posIn2, timestamp = 1000)

        val e7 = edit(pos = posIn2, timestamp = 500, isSynced = true)

        dao.addAll(e1, e2, e3, e4, e5, e6, e7)

        assertEquals(listOf(posIn1, posIn2), dao.getAllUnsyncedPositions(BoundingBox(0.0, 0.0, 1.0, 2.0)))
    }

    @Test fun markSynced() {
        val edit = edit(isSynced = false)
        dao.add(edit)
        val id = edit.id
        assertFalse(dao.get(id)!!.isSynced)
        dao.markSynced(id)
        assertTrue(dao.get(id)!!.isSynced)
    }

    @Test fun peekUnsynced() {
        assertNull(dao.getOldestUnsynced())

        val e1 = edit(isSynced = true)
        dao.add(e1)
        assertNull(dao.getOldestUnsynced())

        val e2 = edit(timestamp = 1000, isSynced = false)
        dao.add(e2)
        assertEquals(e2, dao.getOldestUnsynced())

        val e3 = edit(timestamp = 1500, isSynced = false)
        dao.add(e3)
        assertEquals(e2, dao.getOldestUnsynced())

        val e4 = edit(timestamp = 500, isSynced = false)
        dao.add(e4)
        assertEquals(e4, dao.getOldestUnsynced())
    }

    @Test fun getUnsyncedCount() {
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(edit(isSynced = true))
        assertEquals(0, dao.getUnsyncedCount())

        dao.add(edit(isSynced = false))
        assertEquals(1, dao.getUnsyncedCount())

        dao.add(edit(isSynced = false))
        assertEquals(2, dao.getUnsyncedCount())
    }

    @Test fun getSyncedOlderThan() {
        val oldEnough = edit(timestamp = 500, isSynced = true)
        val tooYoung = edit(timestamp = 1000, isSynced = true)
        val notSynced = edit(timestamp = 500, isSynced = false)
        val imageNotActivatedSynced = edit(timestamp = 500, isSynced = true, imagePaths = listOf("a"))

        dao.addAll(oldEnough, tooYoung, notSynced, imageNotActivatedSynced)

        assertEquals(listOf(oldEnough), dao.getSyncedOlderThan(1000))
    }

    @Test fun updateNoteId() {
        assertEquals(0, dao.updateNoteId(-5, 6))

        val e1 = edit(noteId = -5)
        val e2 = edit(noteId = -5)
        dao.addAll(e1, e2)

        assertEquals(2, dao.updateNoteId(-5, 6))
        assertEquals(6, dao.get(e1.id)!!.noteId)
        assertEquals(6, dao.get(e2.id)!!.noteId)
    }

    @Test fun markImagesActivated() {
        val edit = edit(isSynced = true, imagePaths = listOf("a", "b"))
        dao.add(edit)
        val id = edit.id
        assertTrue(dao.get(id)!!.imagesNeedActivation)
        dao.markImagesActivated(id)
        assertFalse(dao.get(id)!!.imagesNeedActivation)
    }

    @Test fun peekNeedingImagesActivation() {
        assertNull(dao.getOldestNeedingImagesActivation())

        val e1 = edit(isSynced = true, imagePaths = listOf())
        dao.add(e1)
        assertNull(dao.getOldestNeedingImagesActivation())

        val e2 = edit(timestamp = 1000, isSynced = true, imagePaths = listOf("a"))
        dao.add(e2)
        assertEquals(e2, dao.getOldestNeedingImagesActivation())

        val e3 = edit(timestamp = 1500, isSynced = true, imagePaths = listOf("a"))
        dao.add(e3)
        assertEquals(e2, dao.getOldestNeedingImagesActivation())

        val e4 = edit(timestamp = 500, isSynced = true, imagePaths = listOf("a"))
        dao.add(e4)
        assertEquals(e4, dao.getOldestNeedingImagesActivation())
    }

    @Test fun replaceTextInUnsynced() {
        dao.add(edit(text = "test123 jo mama"))
        dao.add(edit(text = "test123 jo mama", isSynced = true))

        dao.replaceTextInUnsynced("123", "456")

        assertEquals("test456 jo mama", dao.get(1L)?.text)
        assertEquals("test123 jo mama", dao.get(2L)?.text)
    }
}

private fun NoteEditsDao.addAll(vararg edits: NoteEdit) = edits.forEach { add(it) }

private fun edit(
    noteId: Long = 1L,
    action: NoteEditAction = NoteEditAction.COMMENT,
    text: String = "test123",
    imagePaths: List<String> = emptyList(),
    pos: LatLon = LatLon(1.0, 1.0),
    timestamp: Long = 123L,
    isSynced: Boolean = false,
    track: List<Trackpoint> = emptyList()
) = NoteEdit(
    1L,
    noteId,
    pos,
    action,
    text,
    imagePaths,
    timestamp,
    isSynced,
    imagePaths.isNotEmpty(),
    track
)
