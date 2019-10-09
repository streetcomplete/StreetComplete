package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences

import java.util.ArrayList
import java.util.Arrays

import javax.inject.Inject

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry

class QuestTypeOrderList @Inject constructor(
    private val prefs: SharedPreferences,
    private val questTypeRegistry: QuestTypeRegistry
) {

    private var orderList: List<List<String>>? = null

    private val asQuestTypeLists: List<List<QuestType<*>>>
        get() {
            val stringLists = get()
            val result = ArrayList<List<QuestType<*>>>(stringLists!!.size)
            for (stringList in stringLists) {
                val questTypes = ArrayList<QuestType<*>>(stringList.size)
                for (string in stringList) {
                    val qt = questTypeRegistry.getByName(string)
                    if (qt != null) questTypes.add(qt)
                }
                if (questTypes.size > 1) {
                    result.add(questTypes)
                }
            }
            return result
        }

    /** Apply and save a user defined order  */
    @Synchronized
    fun apply(before: QuestType<*>, after: QuestType<*>) {
        val lists = get()
        applyOrderItemTo(before, after, lists)
        save(lists!!)
    }

    /** Sort given list by the user defined order  */
    @Synchronized
    fun sort(questTypes: MutableList<QuestType<*>>) {
        val orderLists = asQuestTypeLists
        for (list in orderLists) {
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

    private fun get(): MutableList<List<String>>? {
        if (orderList == null) {
            orderList = load()
        }
        return orderList
    }

    private fun load(): List<List<String>> {
        val order = prefs.getString(Prefs.QUEST_ORDER, null)
        if (order != null) {
            val lists = order.split(DELIM1.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val result = ArrayList<List<String>>(lists.size)
            for (list in lists) {
                result.add(ArrayList(Arrays.asList(*list.split(DELIM2.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())))
            }
            return result
        }
        return ArrayList()
    }

    private fun save(lists: List<List<String>>) {
        val sb = StringBuilder()
        var firstList = true
        for (list in lists) {
            if (firstList)
                firstList = false
            else
                sb.append(DELIM1)
            var firstName = true
            for (name in list) {
                if (firstName)
                    firstName = false
                else
                    sb.append(DELIM2)
                sb.append(name)
            }
        }
        val order = if (sb.length == 0) null else sb.toString()
        prefs.edit().putString(Prefs.QUEST_ORDER, order).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(Prefs.QUEST_ORDER).apply()
        orderList = null
    }

    companion object {

        private fun applyOrderItemTo(before: QuestType<*>, after: QuestType<*>, lists: MutableList<MutableList<String>>?) {
            val beforeName = before.javaClass.simpleName
            val afterName = after.javaClass.simpleName

            // 1. remove after-item from the list it is in
            val afterList = findListThatContains(afterName, lists!!)

            var afterNames: MutableList<String> = ArrayList(2)
            afterNames.add(afterName)

            if (afterList != null) {
                val afterIndex = afterList.indexOf(afterName)
                val beforeList = findListThatContains(beforeName, lists)
                // if it is the head of a list, transplant the whole list
                if (afterIndex == 0 && afterList !== beforeList) {
                    afterNames = afterList
                    lists.remove(afterList)
                } else {
                    afterList.removeAt(afterIndex)
                    // remove that list if it became too small to be meaningful
                    if (afterList.size < 2) lists.remove(afterList)
                }
            }

            // 2. add it/them back to a list after before-item
            val beforeList = findListThatContains(beforeName, lists)

            if (beforeList != null) {
                val beforeIndex = beforeList.indexOf(beforeName)
                beforeList.addAll(beforeIndex + 1, afterNames)
            } else {
                val list = ArrayList<String>()
                list.add(beforeName)
                list.addAll(afterNames)
                lists.add(list)
            }
        }

        private fun findListThatContains(name: String, lists: MutableList<MutableList<String>>)
	        = lists.find { it.contains(name) }

        private val DELIM1 = ";"
        private val DELIM2 = ","
    }
}
