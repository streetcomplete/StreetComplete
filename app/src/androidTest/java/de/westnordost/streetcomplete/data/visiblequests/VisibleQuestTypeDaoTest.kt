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
        assertTrue(dao.get("something"))
    }

    @Test fun disableQuest() {
        dao.put("no", false)
        assertFalse(dao.get("no"))
    }

    @Test fun enableQuest() {
        dao.put("no", false)
        dao.put("no", true)
        assertTrue(dao.get("no"))
    }

    @Test fun reset() {
        dao.put("blurb", false)
        assertFalse(dao.get("blurb"))
        dao.clear()
        assertTrue(dao.get("blurb"))
    }
}
