package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteQuestsHiddenDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NoteQuestsHiddenDao

    @BeforeTest fun createDao() {
        dao = NoteQuestsHiddenDao(database)
    }

    @Test fun addGetDelete() {
        assertFalse(dao.delete(123L))
        dao.add(123L)
        assertNotNull(dao.getTimestamp(123L))
        assertTrue(dao.delete(123L))
        assertNull(dao.getTimestamp(123L))
    }

    @Test fun getAll() {
        dao.add(1L)
        dao.add(2L)
        assertEquals(
            setOf(1L, 2L),
            dao.getAll().map { it.noteId }.toSet()
        )
    }

    @Test fun getNewerThan() = runBlocking {
        dao.add(1L)
        delay(200)
        val time = nowAsEpochMilliseconds()
        dao.add(2L)
        val result = dao.getNewerThan(time - 100).single()
        assertEquals(2L, result.noteId)
    }

    @Test fun deleteAll() {
        assertEquals(0, dao.deleteAll())
        dao.add(1L)
        dao.add(2L)
        assertEquals(2, dao.deleteAll())
        assertNull(dao.getTimestamp(1L))
        assertNull(dao.getTimestamp(2L))
    }

    @Test fun countAll() {
        assertEquals(0, dao.countAll())
        dao.add(3L)
        assertEquals(1, dao.countAll())
    }
}
