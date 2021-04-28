package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.meta.getLastCheckDateKeys
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ktx.toLocalDate
import java.time.Instant
import java.time.LocalDate

abstract class CompareTagAge(val key: String, val dateFilter: DateFilter) : ElementFilter {
    val date: LocalDate get() = dateFilter.date

    override fun toOverpassQLString(): String {
        val dateStr = date.toCheckDateString()
        val datesToCheck = (listOf("timestamp()") + getLastCheckDateKeys(key).map { "t['$it']" })
        val oqlEvaluators = datesToCheck.joinToString(" || ") { "date($it) $operator date('$dateStr')" }
        return "(if: $oqlEvaluators)"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val timestampEdited = obj?.timestampEdited ?: return false
        if (compareTo(Instant.ofEpochMilli(timestampEdited).toLocalDate())) return true

        return getLastCheckDateKeys(key)
            .mapNotNull { obj.tags[it]?.toCheckDate() }
            .any { compareTo(it) }
    }

    abstract fun compareTo(tagValue: LocalDate): Boolean
    abstract val operator: String
}
