package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import de.westnordost.streetcomplete.data.quest.TestQuestTypeD
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class QuestTypeOrderControllerTest {
    private lateinit var questTypeOrderDao: QuestTypeOrderDao
    private lateinit var questPresetsSource: QuestPresetsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var ctrl: QuestTypeOrderController
    private lateinit var listener: QuestTypeOrderSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()
    private val questC = TestQuestTypeC()
    private val questD = TestQuestTypeD()

    @BeforeTest fun setUp() {
        questTypeOrderDao = mock()
        questPresetsSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to questA,
            1 to questB,
            2 to questC,
            3 to questD
        ))

        on(questPresetsSource.addListener(any())).then { invocation ->
            questPresetsListener = (invocation.arguments[0] as QuestPresetsSource.Listener)
            Unit
        }

        on(questPresetsSource.selectedId).thenReturn(0)

        ctrl = QuestTypeOrderController(questTypeOrderDao, questPresetsSource, questTypeRegistry)

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
        assertContentEquals(
            listOf(questD, questC, questB, questA),
            list
        )
    }

    @Test fun getOrders() {
        on(questTypeOrderDao.getAll(0)).thenReturn(listOf(
            questA.name to questB.name,
            questC.name to questD.name
        ))
        assertEquals(
            listOf(questA to questB, questC to questD),
            ctrl.getOrders()
        )
    }

    @Test fun setOrders() {
        ctrl.setOrders(listOf(questA to questB, questC to questD))
        verify(questTypeOrderDao).setAll(0, listOf(
            questA.name to questB.name,
            questC.name to questD.name
        ))
        verify(listener).onQuestTypeOrdersChanged()
    }

    @Test fun `setOrders on not selected preset`() {
        ctrl.setOrders(listOf(questA to questB, questC to questD), 1)
        verify(questTypeOrderDao).setAll(1, listOf(
            questA.name to questB.name,
            questC.name to questD.name
        ))
        verifyNoInteractions(listener)
    }

    @Test fun `add order item`() {
        ctrl.addOrderItem(questA, questB)
        verify(questTypeOrderDao).put(0, questA.name to questB.name)
        verify(listener).onQuestTypeOrderAdded(questA, questB)
    }

    @Test fun `add order item on not selected preset`() {
        ctrl.addOrderItem(questA, questB, 1)
        verify(questTypeOrderDao).put(1, questA.name to questB.name)
        verifyNoInteractions(listener)
    }

    @Test fun clear() {
        ctrl.clear()
        verify(questTypeOrderDao).clear(0)
        verify(listener).onQuestTypeOrdersChanged()
    }

    @Test fun `clear not selected preset`() {
        ctrl.clear(1)
        verify(questTypeOrderDao).clear(1)
        verifyNoInteractions(listener)
    }
}
