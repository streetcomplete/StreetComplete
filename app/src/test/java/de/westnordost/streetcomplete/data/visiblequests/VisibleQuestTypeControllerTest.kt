package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeDisabled
import de.westnordost.streetcomplete.testutils.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class VisibleQuestTypeControllerTest {
    private lateinit var ctrl: VisibleQuestTypeController
    private lateinit var db : VisibleQuestTypeDao

    private val testQuestType = TestQuestTypeA()
    private val disabledTestQuestType = TestQuestTypeDisabled()

    @Before fun createDao() {
        db = mock()
        ctrl = VisibleQuestTypeController(db)
    }

    @Test fun `quest types are visible by default`() {
        assertTrue(ctrl.isVisible(testQuestType))
    }

    @Test fun `by-default-disabled quest types are invisible by default`() {
        assertFalse(ctrl.isVisible(disabledTestQuestType))
    }

    @Test fun `make quest type invisible`() {
        val listener = mock<VisibleQuestTypeSource.Listener>()
        ctrl.addListener(listener)

        ctrl.setVisible(testQuestType, false)
        assertFalse(ctrl.isVisible(testQuestType))

        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun `make by-default-disabled quest type visible`() {
        val listener = mock<VisibleQuestTypeSource.Listener>()
        ctrl.addListener(listener)

        ctrl.setVisible(disabledTestQuestType, true)
        assertTrue(ctrl.isVisible(disabledTestQuestType))

        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun reset() {
        ctrl.setVisible(testQuestType, false)
        assertFalse(ctrl.isVisible(testQuestType))

        val listener = mock<VisibleQuestTypeSource.Listener>()
        ctrl.addListener(listener)

        ctrl.clear()
        assertTrue(ctrl.isVisible(testQuestType))

        verify(listener).onQuestTypeVisibilitiesChanged()
    }
}
