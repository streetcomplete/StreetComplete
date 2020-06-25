package de.westnordost.streetcomplete.data.visiblequests


import android.content.Context

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType2
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType3
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType4
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType5

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.assertj.core.api.Assertions.*
import org.junit.Assert.assertEquals

class QuestTypeOrderListTest {
    private val one = TestQuestType()
    private val two = TestQuestType2()
    private val three = TestQuestType3()
    private val four = TestQuestType4()
    private val five = TestQuestType5()

    private lateinit var list: MutableList<QuestType<*>>
    private lateinit var questTypeOrderList: QuestTypeOrderList

    @Before fun setUpList() {
        list = mutableListOf(one, two, three, four, five)

        questTypeOrderList = QuestTypeOrderList(
            getInstrumentation().context.getSharedPreferences("Test", Context.MODE_PRIVATE),
                QuestTypeRegistry(list)
        )
        questTypeOrderList.clear()
    }

    @Test fun simpleReorder() {
        questTypeOrderList.apply(two, one)
        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(two, one)
    }

    @Test fun twoSeparateOrderLists() {
        questTypeOrderList.apply(two, one)
        questTypeOrderList.apply(five, four)

        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(two, one)
        assertThat(list).containsSequence(five, four)
    }

    @Test fun extendOrderList() {
        questTypeOrderList.apply(three, two)
        questTypeOrderList.apply(two, one)
        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(three, two, one)
    }

    @Test fun extendOrderListInReverse() {
        questTypeOrderList.apply(two, one)
        questTypeOrderList.apply(three, two)
        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(three, two, one)
    }

    @Test fun clear() {
        questTypeOrderList.apply(two, one)
        questTypeOrderList.clear()
        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(one, two)
    }

    @Test fun questTypeInOrderListButNotInToBeSortedList() {
        list.remove(three)
        questTypeOrderList.apply(three, one)
        val before = list.toList()
        questTypeOrderList.sort(list)
        assertEquals(before, list)
    }

    @Test fun questTypeInOrderListButNotInToBeSortedListDoesNotInhibitSorting() {
        list.remove(three)
        questTypeOrderList.apply(three, two)
        questTypeOrderList.apply(two, one)
        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(two, one)
    }

    @Test fun questTypeOrderIsUpdatedCorrectlyMovedDown() {
        questTypeOrderList.apply(three, two)
        questTypeOrderList.apply(two, one)
        // this now conflicts with the first statement -> should move the 3 after the 1
        questTypeOrderList.apply(one, three)

        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(two, one, three)
    }

    @Test fun questTypeOrderIsUpdatedCorrectlyMovedUp() {
        questTypeOrderList.apply(three, two)
        questTypeOrderList.apply(two, one)
        // this now conflicts with the first statement -> should move the 1 before the 2
        questTypeOrderList.apply(three, one)

        questTypeOrderList.sort(list)

        assertThat(list).containsSequence(three, one, two)
    }

    @Test fun pickQuestTypeOrders() {
        questTypeOrderList.apply(four, three)
        questTypeOrderList.apply(two, one)
        questTypeOrderList.apply(one, five)

        // merging the two here..
        questTypeOrderList.apply(one, three)

        questTypeOrderList.sort(list)
        assertThat(list).containsSequence(two, one, three, five)
    }

    @Test fun mergeQuestTypeOrders() {
        questTypeOrderList.apply(four, three)
        questTypeOrderList.apply(two, one)

        // merging the two here..
        questTypeOrderList.apply(three, two)

        questTypeOrderList.sort(list)
        assertThat(list).containsSequence(four, three, two, one)
    }

    @Test fun reorderFirstItemToBackOfSameList() {
        questTypeOrderList.apply(one, two)
        questTypeOrderList.apply(two, three)
        questTypeOrderList.apply(three, four)

        questTypeOrderList.apply(four, one)

        questTypeOrderList.sort(list)
        assertThat(list).containsSequence(two, three, four, one)
    }

    @Test fun reorderAnItemToBackOfSameList() {
        questTypeOrderList.apply(one, two)
        questTypeOrderList.apply(two, three)
        questTypeOrderList.apply(three, four)

        questTypeOrderList.apply(four, two)

        questTypeOrderList.sort(list)
        assertThat(list).containsSequence(one, three, four, two)
    }
}
