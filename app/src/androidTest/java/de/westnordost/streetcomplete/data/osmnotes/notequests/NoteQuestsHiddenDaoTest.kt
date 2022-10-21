package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteQuestsHiddenDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NoteQuestsHiddenDao

    @Before fun createDao() {
        dao = NoteQuestsHiddenDao(database)
    }

    @Test fun getButNothingIsThere() {
        assertFalse(dao.contains(123L))
    }

    @Test fun addAndGet() {
        dao.add(123L)
        assertTrue(dao.contains(123L))
    }

    @Test fun addGetDelete() {
        assertFalse(dao.delete(123L))
        dao.add(123L)
        assertTrue(dao.delete(123L))
        assertFalse(dao.contains(123L))
    }

    @Test fun getAllIds() {
        dao.add(1L)
        dao.add(2L)
        assertTrue(dao.getAllIds().containsExactlyInAnyOrder(listOf(1L, 2L)))
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
        assertFalse(dao.contains(1L))
        assertFalse(dao.contains(2L))
    }
}
