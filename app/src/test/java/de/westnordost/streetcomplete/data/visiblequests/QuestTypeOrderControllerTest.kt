package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import de.westnordost.streetcomplete.data.quest.TestQuestTypeD
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class QuestTypeOrderControllerTest {
    @Mock private lateinit var questTypeOrderDao: QuestTypeOrderDao
    @Mock private lateinit var questPresetsSource: QuestPresetsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var ctrl: QuestTypeOrderController
    @Mock private lateinit var listener: QuestTypeOrderSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()
    private val questC = TestQuestTypeC()
    private val questD = TestQuestTypeD()

    @BeforeTest fun setUp() {
        questTypeOrderDao = mock(classOf<QuestTypeOrderDao>())
        questPresetsSource = mock(classOf<QuestPresetsSource>())
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to questA,
            1 to questB,
            2 to questC,
            3 to questD
        ))

        on { questPresetsSource.addListener(any()) }.invokes { arguments ->
            questPresetsListener = arguments[0] as QuestPresetsSource.Listener
            Unit
        }

        on { questPresetsSource.selectedId }.returns(0)

        ctrl = QuestTypeOrderController(questTypeOrderDao, questPresetsSource, questTypeRegistry)

        listener = mock(classOf<QuestTypeOrderSource.Listener>())
        ctrl.addListener(listener)
    }

    @Test fun `notifies listener when changing quest preset`() {
        questPresetsListener.onSelectedQuestPresetChanged()
        verifyInvokedExactlyOnce { listener.onQuestTypeOrdersChanged() }
    }

    @Test fun sort() {
        val list = mutableListOf<QuestType>(questA, questB, questC, questD)
        on { questTypeOrderDao.getAll(0) }.returns(listOf(
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
        on { questTypeOrderDao.getAll(0) }.returns(listOf(
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
        verifyInvokedExactlyOnce { questTypeOrderDao.setAll(0, listOf(
            questA.name to questB.name,
            questC.name to questD.name
        )) }
        verifyInvokedExactlyOnce { listener.onQuestTypeOrdersChanged() }
    }

    @Test fun `setOrders on not selected preset`() {
        ctrl.setOrders(listOf(questA to questB, questC to questD), 1)
        verifyInvokedExactlyOnce { questTypeOrderDao.setAll(1, listOf(
            questA.name to questB.name,
            questC.name to questD.name
        ) ) }
        // verifyNoInteractions(listener)
    }

    @Test fun `add order item`() {
        ctrl.addOrderItem(questA, questB)
        verifyInvokedExactlyOnce { questTypeOrderDao.put(0, questA.name to questB.name) }
        verifyInvokedExactlyOnce { listener.onQuestTypeOrderAdded(questA, questB) }
    }

    @Test fun `add order item on not selected preset`() {
        ctrl.addOrderItem(questA, questB, 1)
        verifyInvokedExactlyOnce { questTypeOrderDao.put(1, questA.name to questB.name) }
        // verifyNoInteractions(listener)
    }

    @Test fun clear() {
        ctrl.clear()
        verifyInvokedExactlyOnce { questTypeOrderDao.clear(0) }
        verifyInvokedExactlyOnce { listener.onQuestTypeOrdersChanged() }
    }

    @Test fun `clear not selected preset`() {
        ctrl.clear(1)
        verifyInvokedExactlyOnce { questTypeOrderDao.clear(1) }
        // verifyNoInteractions(listener)
    }
}
