package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.minusInSystemTimeZone
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Returns the date x days in the past */
fun dateDaysAgo(daysAgo: Float): LocalDate =
    LocalDateTime.now().minusInSystemTimeZone((daysAgo * 24).toLong(), DateTimeUnit.HOUR).date

fun ElementFilter.matches(tags: Map<String, String>, date: LocalDate? = null): Boolean =
    matches(node(tags = tags, timestamp = date?.toEpochMilli()))
