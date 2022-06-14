package de.westnordost.streetcomplete.data.user.statistics

interface StatisticsSource {

    interface Listener {
        /** Called when the given edit type has been done once */
        fun onAddedOne(type: String)
        /** Called when the given edit type has been undone once */
        fun onSubtractedOne(type: String)
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
    fun getEditCount(): Int

    /** Return amount of edits of the given type done */
    fun getEditCount(type: String): Int

    /** Return amount of edits of the given types done */
    fun getEditCount(types: List<String>): Int

    /** Return all edit type statistics */
    fun getEditTypeStatistics(): List<EditTypeStatistics>

    /** Return all country statistics */
    fun getCountryStatistics(): List<CountryStatistics>

    /** Return the country statistics of the country in which the user solved the most quests, if any */
    fun getCountryStatisticsOfCountryWithBiggestSolvedCount(): CountryStatistics?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
