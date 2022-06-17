package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import de.westnordost.streetcomplete.data.quest.TestQuestTypeD
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class QuestTypeOrderControllerTest {
    private lateinit var questTypeOrderDao: QuestTypeOrderDao
    private lateinit var questPresetsSource: QuestPresetsSource
    private lateinit var ctrl: QuestTypeOrderController
    private lateinit var listener: QuestTypeOrderSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()
    private val questC = TestQuestTypeC()
    private val questD = TestQuestTypeD()

    @Before fun setUp() {
        questTypeOrderDao = mock()
        questPresetsSource = mock()

        on(questPresetsSource.addListener(any())).then { invocation ->
            questPresetsListener = (invocation.arguments[0] as QuestPresetsSource.Listener)
            Unit
        }

        on(questPresetsSource.selectedId).thenReturn(0)

        ctrl = QuestTypeOrderController(questTypeOrderDao, questPresetsSource)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `notifies listener when changing quest preset`() {
        questPresetsListener.onSelectedQuestPresetChanged()
        verify(listener).onQuestTypeOrdersChanged()
    }

    @Test fun sort() {
        val list = mutableListOf<QuestType>(questA, questB, questC, questD)
        on(questTypeOrderDao.getAll(0)).thenReturn(listOf(
            // A,B,C,D -> A,D,B,C
            questD.name to questA.name,
            // A,D,B,C -> A,D,C,B
            questC.name to questD.name,
            // A,D,C,B -> D,C,B,A
            questA.name to questB.name
        ))

        ctrl.sort(list)
        assertEquals(
            listOf(questD, questC, questB, questA),
            list
        )
    }

    @Test fun `adding order item`() {
        ctrl.addOrderItem(questA, questB)
        verify(questTypeOrderDao).put(0, questA.name to questB.name)
        verify(listener).onQuestTypeOrderAdded(questA, questB)
    }

    @Test fun `clear orders`() {
        ctrl.clear()
        verify(questTypeOrderDao).clear(0)
        verify(listener).onQuestTypeOrdersChanged()
    }
}
