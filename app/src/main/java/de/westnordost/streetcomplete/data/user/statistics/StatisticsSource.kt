package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.quest.QuestType

interface StatisticsSource {

    interface Listener {
        /** Called when the given quest type has been solved once */
        fun onAddedOne(questType: QuestType<*>)
        /** Called when the given quest type has been undone once */
        fun onSubtractedOne(questType: QuestType<*>)
        /** Called when all the statistics have been replaced by a new set of statistics. E.g. when
         *  the updated statistics have been downloaded from the server */
        fun onUpdatedAll()
        /** Called when the statistics have been cleared. E.g. on logout. */
        fun onCleared()
        /** Called when the days the user has been active changed */
        fun onUpdatedDaysActive()
    }

    /** Users' global rank. If <= 0, it's not set yet */
    val rank: Int
    /** Number of days the user solved quests in this app */
    val daysActive: Int
    /** Whether the statistics are still being synchronized with the backend */
    val isSynchronizing: Boolean

    /** Return the total amount of quests solved*/
    fun getSolvedCount(): Int

    /** Return amount of quests of the given type solved */
    fun getSolvedCount(questType: QuestType<*>): Int

    /** Return amount of quests of the given types solved */
    fun getSolvedCount(questTypes: List<QuestType<*>>): Int

    /** Return all quest statistics */
    fun getQuestStatistics(): List<QuestTypeStatistics>

    /** Return all country statistics */
    fun getCountryStatistics(): List<CountryStatistics>

    /** Return the country statistics of the country in which the user solved the most quests, if any */
    fun getCountryStatisticsOfCountryWithBiggestSolvedCount(): CountryStatistics?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
