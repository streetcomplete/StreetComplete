package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class QuestProfilesControllerTest {

    private lateinit var questProfilesDao: QuestProfilesDao
    private lateinit var selectedQuestProfileStore: SelectedQuestProfileStore
    private lateinit var ctrl: QuestProfilesController
    private lateinit var listener: QuestProfilesSource.Listener

    private val profile = QuestProfile(1, "test")

    @Before fun setUp() {
        questProfilesDao = mock()
        selectedQuestProfileStore = mock()
        ctrl = QuestProfilesController(questProfilesDao, selectedQuestProfileStore)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun get() {
        on(questProfilesDao.getName(1)).thenReturn("huhu")
        on(selectedQuestProfileStore.get()).thenReturn(1)
        assertEquals("huhu", ctrl.selectedQuestProfileName)
    }

    @Test fun getAll() {
        on(questProfilesDao.getAll()).thenReturn(listOf(profile))
        assertEquals(listOf(profile), ctrl.getAllQuestProfiles())
    }

    @Test fun add() {
        on(questProfilesDao.add(any())).thenReturn(123)
        ctrl.addQuestProfile("test")
        verify(questProfilesDao).add("test")
        verify(listener).onAddedQuestProfile(QuestProfile(123, "test"))
    }

    @Test fun delete() {
        ctrl.deleteQuestProfile(123)
        verify(questProfilesDao).delete(123)
        verify(listener).onDeletedQuestProfile(123)
    }

    @Test fun `delete current profile switches to profile 0`() {
        on(ctrl.selectedQuestProfileId).thenReturn(55)
        ctrl.deleteQuestProfile(55)
        verify(questProfilesDao).delete(55)
        verify(listener).onDeletedQuestProfile(55)
        verify(selectedQuestProfileStore).set(0)
    }

    @Test fun `change current profile`() {
        ctrl.selectedQuestProfileId = 11
        verify(selectedQuestProfileStore).set(11)
        verify(listener).onSelectedQuestProfileChanged()
    }
}
