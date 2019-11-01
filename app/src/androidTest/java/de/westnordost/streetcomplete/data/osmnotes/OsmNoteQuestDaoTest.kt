package de.westnordost.streetcomplete.data.osmnotes

import org.junit.Before
import org.junit.Test

import java.util.Date
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note

import org.junit.Assert.*

class OsmNoteQuestDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmNoteQuestDao
    private lateinit var noteDao: NoteDao
    private lateinit var questType: OsmNoteQuestType

    @Before fun createDao() {
        questType = OsmNoteQuestType()
        val noteMapping = NoteMapping(serializer)
        dao = OsmNoteQuestDao(dbHelper, OsmNoteQuestMapping(serializer, questType, noteMapping))
        noteDao = NoteDao(dbHelper, noteMapping)
    }

    @Test fun addGetNoChanges() {
        val quest = create()
        addToDaos(quest)
        checkEqual(quest, dao.get(quest.id!!))
    }

    @Test fun addGetWithChanges() {
        val quest = create(
            status = QuestStatus.ANSWERED,
            comment = "hi da du",
            imagePaths = listOf("blubbi", "diblub")
        )
        addToDaos(quest)

        checkEqual(quest, dao.get(quest.id!!))
    }

    @Test fun deleteButNothingIsThere() {
        assertFalse(dao.delete(1L))
    }

    @Test fun addAndDelete() {
        val quest = create()
        addToDaos(quest)

        assertTrue(dao.delete(quest.id!!))
        assertNull(dao.get(quest.id!!))
        assertFalse(dao.delete(quest.id!!))
    }

    @Test fun update() {
        val quest = create()
        addToDaos(quest)

        quest.status = QuestStatus.HIDDEN
        quest.comment = "ho"
        quest.imagePaths = listOf("bla", "blu")

        dao.update(quest)

        checkEqual(quest, dao.get(quest.id!!))
    }

    @Test fun addAllAndDeleteAll() {
        val notes = listOf(createNote(1), createNote(2), createNote(3))
        noteDao.putAll(notes)

        val quests = notes.map { create(it) }
        assertEquals(3, dao.addAll(quests))

        for (quest in quests) {
            assertNotNull(quest.id)
            checkEqual(quest, dao.get(quest.id!!))
        }
        assertEquals(3, dao.deleteAllIds(quests.map { it.id!! }))
        assertEquals(0, dao.getCount())
    }

    @Test fun addSameNoteTwiceDoesntWork() {
        val note = createNote()
        noteDao.put(note)
        dao.add(create(note, status = QuestStatus.NEW))

        val questForSameNote = create(note, QuestStatus.HIDDEN)
        assertFalse(dao.add(questForSameNote))

        val quests = dao.getAll()
        assertEquals(QuestStatus.NEW, quests.single().status)
        assertNull(questForSameNote.id)
    }

    @Test fun replaceSameNoteDoesWork() {
        val note = createNote()
        noteDao.put(note)
        dao.add(create(note, QuestStatus.NEW))

        val questForSameNote = create(note, QuestStatus.HIDDEN)
        assertTrue(dao.replace(questForSameNote))

        val quests = dao.getAll()
        assertEquals(QuestStatus.HIDDEN, quests.single().status)
        assertNotNull(questForSameNote.id)
    }

    @Test fun getAllPositions() {
        addToDaos(create(notePosition = OsmLatLon(34.0, 35.0)))
        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 50.0, 50.0))
        assertEquals(OsmLatLon(34.0, 35.0), positions.single())
    }

    @Test fun getAllByBBox() {
        addToDaos(
            create(noteId = 1, notePosition = OsmLatLon(5.0, 5.0)),
            create(noteId = 2, notePosition = OsmLatLon(11.0, 11.0))
        )

        assertEquals(1, dao.getAll(bounds = BoundingBox(0.0, 0.0, 10.0, 10.0)).size)
        assertEquals(2, dao.getAll().size)
    }

    @Test fun getAllByStatus() {
        addToDaos(
            create(noteId = 1, status = QuestStatus.HIDDEN),
            create(noteId = 2, status = QuestStatus.NEW)
        )

        assertEquals(1, dao.getAll(statusIn = listOf(QuestStatus.HIDDEN)).size)
        assertEquals(1, dao.getAll(statusIn = listOf(QuestStatus.NEW)).size)
        assertEquals(0, dao.getAll(statusIn = listOf(QuestStatus.CLOSED)).size)
        assertEquals(2, dao.getAll(statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN)).size)
    }

    @Test fun getCount() {
        addToDaos(create(1), create(2))
        assertEquals(2, dao.getCount())
    }

    @Test fun deleteAll() {
        addToDaos(create(1), create(2))
        assertEquals(2, dao.deleteAll())
    }

    private fun checkEqual(quest: OsmNoteQuest, dbQuest: OsmNoteQuest?) {
        assertNotNull(dbQuest)
        assertEquals(quest.lastUpdate, dbQuest!!.lastUpdate)
        assertEquals(quest.status, dbQuest.status)
        assertEquals(quest.center, dbQuest.center)
        assertEquals(quest.comment, dbQuest.comment)
        assertEquals(quest.id, dbQuest.id)
        assertEquals(quest.type, dbQuest.type)
        // note saving already tested in NoteDaoTest
    }

    private fun createNote(id: Long = 5, position: LatLon = OsmLatLon(1.0, 1.0)) = Note().also {
        it.id = id
        it.position = position
        it.status = Note.Status.OPEN
        it.dateCreated = Date(5000)
    }

    private fun create(
        noteId: Long = 1,
        status: QuestStatus = QuestStatus.NEW,
        notePosition: LatLon = OsmLatLon(1.0, 1.0),
        comment: String? = null,
        imagePaths: List<String>? = null
    ) = create( createNote(noteId, notePosition), status, comment, imagePaths)

    private fun create(
        note: Note,
        status: QuestStatus = QuestStatus.NEW,
        comment: String? = null,
        imagePaths: List<String>? = null
    ) = OsmNoteQuest( null, note, status, comment, Date(5000), questType, imagePaths)

    private fun addToDaos(vararg quests: OsmNoteQuest) {
        for (quest in quests) {
            noteDao.put(quest.note)
            dao.add(quest)
        }
    }
}
