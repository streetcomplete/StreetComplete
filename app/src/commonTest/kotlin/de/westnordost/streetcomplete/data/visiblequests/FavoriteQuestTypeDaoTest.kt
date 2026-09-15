package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseTestCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FavoriteQuestTypeDaoTest : StreetCompleteDatabaseTestCase() {
    private lateinit var dao: FavoriteQuestTypeDao

    override fun onDatabaseInitialized(database: Database) {
        dao = FavoriteQuestTypeDao(database)
    }

    @Test fun getEmpty() {
        assertTrue(dao.getAll(0).isEmpty())
        assertTrue(dao.getAll(1).isEmpty())
        assertNull(dao.get(0, "a"))
    }

    @Test fun putGetOne() {
        dao.put(0, "a")

        assertEquals("a", dao.get(0, "a"))
        assertTrue(dao.getAll(1).isEmpty())
    }

    @Test fun putGetSeveral() {
        dao.put(1, "a")
        dao.put(1, "b")
        dao.put(1, "c")

        assertEquals(listOf("a", "b", "c"), dao.getAll(1))
        assertTrue(dao.getAll(0).isEmpty())
    }

    @Test fun getOnlyFromCorrectPreset() {
        dao.put(0, "a")
        dao.put(1, "b")

        assertEquals("a", dao.get(0, "a"))
        assertEquals("b", dao.get(1, "b"))
        assertNull(dao.get(0, "b"))
        assertNull(dao.get(1, "a"))
    }

    @Test fun remove() {
        dao.put(0, "a")
        dao.put(0, "b")

        dao.remove(0, "a")

        assertNull(dao.get(0, "a"))
        assertEquals(listOf("b"), dao.getAll(0))
    }

    @Test fun removeOnlyFromCorrectPreset() {
        dao.put(0, "a")
        dao.put(1, "a")

        dao.remove(0, "a")

        assertNull(dao.get(0, "a"))
        assertEquals(listOf("a"), dao.getAll(1))
    }

    @Test fun replaceExistingFavorite() {
        dao.put(0, "a")
        dao.put(0, "a")

        assertEquals(listOf("a"), dao.getAll(0))
    }

    @Test fun putAll() {
        dao.putAll(0, listOf("a", "b", "c"))

        assertEquals(
            listOf("a", "b", "c"),
            dao.getAll(0)
        )
        assertTrue(dao.getAll(1).isEmpty())
    }

    @Test fun putAllReplacesExistingFavorites() {
        dao.put(0, "a")
        dao.put(0, "b")

        dao.putAll(0, listOf("c", "d"))

        assertEquals(
            listOf("c", "d"),
            dao.getAll(0)
        )
    }

    @Test fun putAllWithEmptyListClearsAllFavorites() {
        dao.put(0, "a")
        dao.put(0, "b")

        dao.putAll(0, emptyList())

        assertTrue(dao.getAll(0).isEmpty())
    }

    @Test fun clear() {
        dao.put(0, "a")
        dao.put(1, "b")
        dao.put(1, "c")

        dao.clear(1)

        assertEquals(listOf("a"), dao.getAll(0))
        assertTrue(dao.getAll(1).isEmpty())
    }
}
