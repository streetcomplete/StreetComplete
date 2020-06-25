package de.westnordost.streetcomplete.data.visiblequests

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.osmquest.DisabledTestQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType

import org.junit.Assert.*

class VisibleQuestTypeDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: VisibleQuestTypeDao

    private val testQuestType = TestQuestType()
    private val disabledTestQuestType = DisabledTestQuestType()

    @Before fun createDao() {
        dao = VisibleQuestTypeDao(dbHelper)
    }

    @Test fun defaultEnabledQuest() {
        assertTrue(dao.isVisible(testQuestType))
    }

    @Test fun defaultDisabledQuests() {
        assertFalse(dao.isVisible(disabledTestQuestType))
    }

    @Test fun disableQuest() {
        dao.setVisible(testQuestType, false)
        assertFalse(dao.isVisible(testQuestType))
    }

    @Test fun enableQuest() {
        dao.setVisible(disabledTestQuestType, true)
        assertTrue(dao.isVisible(disabledTestQuestType))
    }

    @Test fun reset() {
        dao.setVisible(testQuestType, false)
        assertFalse(dao.isVisible(testQuestType))
        dao.clear()
        assertTrue(dao.isVisible(testQuestType))
    }
}
