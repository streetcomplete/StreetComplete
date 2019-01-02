package de.westnordost.streetcomplete.quests.opening_hours.model

class OpeningWeekdays(var weekdays: Weekdays, var timeRanges: MutableList<TimeRange>) {

    fun isSelfIntersecting(): Boolean {
        for (i in timeRanges.indices) {
            for (j in i + 1 until timeRanges.size) {
                if (timeRanges[i].intersects(timeRanges[j])) return true
            }
        }
        return false
    }

    fun intersects(other: OpeningWeekdays): Boolean {
        val these = if (timeExtendsToNextDay()) splitAtMidnight() else arrayOf(this)
        val others = if (other.timeExtendsToNextDay()) other.splitAtMidnight() else arrayOf(other)

        for (i in these) {
            for (it in others) {
                if (i.intersectsWhenNoTimeExtendsToNextDay(it)) return true
            }
        }
        return false
    }

    private fun intersectsWhenNoTimeExtendsToNextDay(other: OpeningWeekdays): Boolean {
        if (!weekdays.intersects(other.weekdays)) return false

        for (timeRange in timeRanges) {
            for (otherTimeRange in other.timeRanges) {
                if (timeRange.intersects(otherTimeRange)) return true
            }
        }
        return false
    }

    fun intersectsWeekdays(other: OpeningWeekdays) =
        weekdays.intersects(other.weekdays)
            || timeExtendsToNextDay() && createNextDayWeekdays().intersects(other.weekdays)
            || other.timeExtendsToNextDay() && other.createNextDayWeekdays().intersects(weekdays)

    /** for example "20:00-03:00"  */
    private fun timeExtendsToNextDay() = timeRanges.any { it.loops }

    private fun splitAtMidnight(): Array<OpeningWeekdays> {
        val beforeMidnight = mutableListOf<TimeRange>()
        val afterMidnight = mutableListOf<TimeRange>()
        for (timeRange in timeRanges) {
            if (timeRange.loops) {
                beforeMidnight.add(TimeRange(timeRange.start, 24 * 60))
                afterMidnight.add(TimeRange(0, timeRange.end, timeRange.isOpenEnded))
            } else {
                beforeMidnight.add(timeRange)
            }
        }
        return arrayOf(
            OpeningWeekdays(weekdays, beforeMidnight),
            OpeningWeekdays(createNextDayWeekdays(), afterMidnight)
        )
    }

    /** For example creates a "Tu-Th, Su" for a "Mo-We, Sa"  */
    private fun createNextDayWeekdays(): Weekdays {
        val selection = weekdays.selection
        val days = 7
        val result = BooleanArray(days)
        for (i in days - 1 downTo 0) {
            result[i] = selection[if (i > 0) i - 1 else days - 1]
        }
        return Weekdays(result)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpeningWeekdays) return false
        return weekdays == other.weekdays && timeRanges == other.timeRanges
    }

    override fun hashCode() = 31 * weekdays.hashCode() + timeRanges.hashCode()
}
