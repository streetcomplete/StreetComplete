package de.westnordost.streetcomplete.data.visiblequests

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase

import org.junit.Assert.*

class VisibleQuestTypeDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: VisibleQuestTypeDao

    @Before fun createDao() {
        dao = VisibleQuestTypeDao(database)
    }

    @Test fun defaultEnabledQuest() {
        assertTrue(dao.get(0, "something"))
        assertTrue(dao.get(1, "something"))
    }

    @Test fun disableQuest() {
        dao.put(0, "no", false)
        dao.put(1, "blob", false)
        assertFalse(dao.get(0, "no"))
        assertTrue(dao.get(1, "no"))
        assertFalse(dao.get(1, "blob"))
    }

    @Test fun enableQuest() {
        dao.put(0, "no", false)
        dao.put(0, "no", true)
        assertTrue(dao.get(0, "no"))
    }

    @Test fun reset() {
        dao.put(0, "blurb", false)
        dao.put(1, "blurb", false)
        assertFalse(dao.get(0, "blurb"))
        assertFalse(dao.get(1, "blurb"))
        dao.clear(0)
        assertTrue(dao.get(0, "blurb"))
        assertFalse(dao.get(1, "blurb"))
        dao.clear(1)
        assertTrue(dao.get(1, "blurb"))
    }
}
