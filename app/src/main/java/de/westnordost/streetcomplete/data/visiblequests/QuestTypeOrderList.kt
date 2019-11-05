package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import androidx.core.content.edit

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry

class QuestTypeOrderList @Inject constructor(
    private val prefs: SharedPreferences,
    private val questTypeRegistry: QuestTypeRegistry
) {

    private val orderLists: MutableList<MutableList<String>> by lazy { load() }

    private val questTypeOrderLists: List<List<QuestType<*>>> get() =
        orderLists.mapNotNull { orderList ->
            val questTypes = orderList.mapNotNull { questTypeRegistry.getByName(it) }
            if (questTypes.size > 1) questTypes else null
        }

    /** Apply and save a user defined order  */
    @Synchronized fun apply(before: QuestType<*>, after: QuestType<*>) {
        applyOrderItem(before, after)
        save(orderLists)
    }

    /** Sort given list by the user defined order  */
    @Synchronized fun sort(questTypes: MutableList<QuestType<*>>) {
        for (list in questTypeOrderLists) {
            val reorderedQuestTypes = ArrayList<QuestType<*>>(list.size - 1)
            for (questType in list.subList(1, list.size)) {
                if (questTypes.remove(questType)) {
                    reorderedQuestTypes.add(questType)
                }
            }
            val startIndex = questTypes.indexOf(list[0])
            questTypes.addAll(startIndex + 1, reorderedQuestTypes)
        }
    }

    @Synchronized fun clear() {
        orderLists.clear()
        save(orderLists)
    }

    private fun load(): MutableList<MutableList<String>> {
        val order = prefs.getString(Prefs.QUEST_ORDER, null)
        return order?.split(DELIM1)?.map { it.split(DELIM2).toMutableList() }?.toMutableList() ?: mutableListOf()
    }

    private fun save(lists: List<List<String>>) {
        val joined = lists.joinToString(DELIM1) { it.joinToString(DELIM2) }
        prefs.edit { putString(Prefs.QUEST_ORDER, if (joined.isNotEmpty()) joined else null) }
    }

    private fun applyOrderItem(before: QuestType<*>, after: QuestType<*>) {
        val beforeName = before.javaClass.simpleName
        val afterName = after.javaClass.simpleName

        // 1. remove after-item from the list it is in
        val afterList = findListThatContains(afterName)

        var afterNames: MutableList<String> = ArrayList(2)
        afterNames.add(afterName)

        if (afterList != null) {
            val afterIndex = afterList.indexOf(afterName)
            val beforeList = findListThatContains(beforeName)
            // if it is the head of a list, transplant the whole list
            if (afterIndex == 0 && afterList !== beforeList) {
                afterNames = afterList
                orderLists.remove(afterList)
            } else {
                afterList.removeAt(afterIndex)
                // remove that list if it became too small to be meaningful
                if (afterList.size < 2) orderLists.remove(afterList)
            }
        }

        // 2. add it/them back to a list after before-item
        val beforeList = findListThatContains(beforeName)

        if (beforeList != null) {
            val beforeIndex = beforeList.indexOf(beforeName)
            beforeList.addAll(beforeIndex + 1, afterNames)
        } else {
            val list = mutableListOf<String>()
            list.add(beforeName)
            list.addAll(afterNames)
            orderLists.add(list)
        }
    }

    private fun findListThatContains(name: String): MutableList<String>? =
        orderLists.firstOrNull { it.contains(name) }

    companion object {
        private const val DELIM1 = ";"
        private const val DELIM2 = ","
    }
}
