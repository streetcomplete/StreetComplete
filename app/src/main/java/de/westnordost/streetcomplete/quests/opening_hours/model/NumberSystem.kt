package de.westnordost.streetcomplete.quests.opening_hours.model

/** aka number range / Zahlenraum  */
class NumberSystem(private val min: Int, private val max: Int) {

    init {
        if (max < min) throw IllegalArgumentException("min must be smaller or equal to max")
    }

    fun getSize(section: CircularSection): Int {
        val s = Math.max(min, section.start)
        val e = Math.min(section.end, max)
        return if (s <= e) e - s + 1 else max - s + 1 + e - min + 1
    }

    /** @return the complemented of the given ranges
     */
    fun complemented(ranges: Collection<CircularSection>): List<CircularSection> {
        val rangeList = canonicalize(ranges)
        val complementList = mutableListOf<CircularSection>()
        var start = min
        for (range in rangeList) {
            if (range.start > start) {
                complementList.add(CircularSection(start, range.start - 1))
            }
            start = Math.max(start, range.end + 1)
            if (start > max) break
        }
        if (start <= max) complementList.add(CircularSection(start, max))

        mergeFirstAndLastSection(complementList)

        return complementList
    }

    fun merged(ranges: List<CircularSection>): List<CircularSection> {
        val result = ranges.toMutableList()
        result.sort()
        mergeFirstAndLastSection(result)
        return result
    }

    private fun mergeFirstAndLastSection(ranges: MutableList<CircularSection>) {
        if (ranges.size > 1) {
            val lastIndex = ranges.size - 1
            val first = ranges[0]
            val last = ranges[lastIndex]
            if (first.start == min && last.end == max) {
                ranges.removeAt(lastIndex)
                ranges.removeAt(0)
                ranges.add(mergeAlongBounds(first, last))
            }
        }
    }

    private fun mergeAlongBounds(lowerSection: CircularSection, upperSection: CircularSection) =
        CircularSection(upperSection.start, lowerSection.end)

    private fun splitAlongBounds(range: CircularSection): List<CircularSection> {
        val result = mutableListOf<CircularSection>()
        val upperSection = CircularSection(range.start, max)
        if (!upperSection.loops) result.add(upperSection)

        val lowerSections = CircularSection(min, range.end)
        if (!lowerSections.loops) result.add(lowerSections)
        return result
    }

    private fun canonicalize(ranges: Collection<CircularSection>): List<CircularSection> {
        // to calculate with circular StartEnds is so complicated, lets dumb it down here
        val rangeList = mutableListOf<CircularSection>()
        for (range in ranges) {
            if (range.loops) {
                rangeList.addAll(splitAlongBounds(range))
            } else if (min <= range.end || max >= range.start) {
                rangeList.add(range)
            }// leave out those which are not in the max range anyway
        }
        rangeList.sort()
        return rangeList
    }
}
