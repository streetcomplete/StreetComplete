package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.Matcher
import de.westnordost.streetcomplete.data.elementfilter.withOptionalUnitToDoubleOrNull
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys
import de.westnordost.streetcomplete.osm.toCheckDate
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

sealed interface ElementFilter : Matcher<Element> {
    abstract override fun toString(): String
}

class HasKey(val key: String) : ElementFilter {
    override fun toString() = key
    override fun matches(obj: Element) = obj.tags.containsKey(key)
}

class NotHasKey(val key: String) : ElementFilter {
    override fun toString() = "!$key"
    override fun matches(obj: Element) = !obj.tags.containsKey(key)
}

class HasTag(val key: String, val value: String) : ElementFilter {
    override fun toString() = "$key = $value"
    override fun matches(obj: Element) = obj.tags[key] == value
}

class NotHasTag(val key: String, val value: String) : ElementFilter {
    override fun toString() = "$key != $value"
    override fun matches(obj: Element) = obj.tags[key] != value
}

class HasKeyLike(val key: String) : ElementFilter {
    private val regex = RegexOrSet.from(key)

    override fun toString() = "~$key"
    override fun matches(obj: Element) = obj.tags.keys.any { regex.matches(it) }
}

class NotHasKeyLike(val key: String) : ElementFilter {
    private val regex = RegexOrSet.from(key)

    override fun toString() = "!~$key"
    override fun matches(obj: Element) = obj.tags.keys.none { regex.matches(it) }
}

class HasTagValueLike(val key: String, val value: String) : ElementFilter {
    private val regex = RegexOrSet.from(value)

    override fun toString() = "$key ~ $value"
    override fun matches(obj: Element) = obj.tags[key]?.let { regex.matches(it) } ?: false
}

class NotHasTagValueLike(val key: String, val value: String) : ElementFilter {
    private val regex = RegexOrSet.from(value)

    override fun toString() = "$key !~ $value"
    override fun matches(obj: Element) = obj.tags[key]?.let { !regex.matches(it) } ?: true
}

class HasTagLike(val key: String, val value: String) : ElementFilter {
    private val keyRegex = RegexOrSet.from(key)
    private val valueRegex = RegexOrSet.from(value)

    override fun toString() = "~$key ~ $value"
    override fun matches(obj: Element) =
        obj.tags.entries.any { keyRegex.matches(it.key) && valueRegex.matches(it.value) }
}

class HasTagLessThan(key: String, value: Float) : CompareTagValue(key, value) {
    override fun toString() = "$key < $value"
    override fun compareTo(tagValue: Float) = tagValue < value
}
class HasTagGreaterThan(key: String, value: Float) : CompareTagValue(key, value) {
    override fun toString() = "$key > $value"
    override fun compareTo(tagValue: Float) = tagValue > value
}
class HasTagLessOrEqualThan(key: String, value: Float) : CompareTagValue(key, value) {
    override fun toString() = "$key <= $value"
    override fun compareTo(tagValue: Float) = tagValue <= value
}
class HasTagGreaterOrEqualThan(key: String, value: Float) : CompareTagValue(key, value) {
    override fun toString() = "$key >= $value"
    override fun compareTo(tagValue: Float) = tagValue >= value
}

abstract class CompareTagValue(val key: String, val value: Float) : ElementFilter {
    abstract fun compareTo(tagValue: Float): Boolean
    override fun matches(obj: Element): Boolean {
        val tagValue = obj.tags[key]?.withOptionalUnitToDoubleOrNull()?.toFloat() ?: return false
        return compareTo(tagValue)
    }
}

class HasDateTagLessThan(key: String, dateFilter: DateFilter) : CompareDateTagValue(key, dateFilter) {
    override fun toString() = "$key < $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue < dateFilter.date
}
class HasDateTagGreaterThan(key: String, dateFilter: DateFilter) : CompareDateTagValue(key, dateFilter) {
    override fun toString() = "$key > $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue > dateFilter.date
}
class HasDateTagLessOrEqualThan(key: String, dateFilter: DateFilter) : CompareDateTagValue(key, dateFilter) {
    override fun toString() = "$key <= $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue <= dateFilter.date
}
class HasDateTagGreaterOrEqualThan(key: String, dateFilter: DateFilter) : CompareDateTagValue(key, dateFilter) {
    override fun toString() = "$key >= $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue >= dateFilter.date
}

abstract class CompareDateTagValue(val key: String, val dateFilter: DateFilter) : ElementFilter {
    abstract fun compareTo(tagValue: LocalDate): Boolean
    override fun matches(obj: Element): Boolean {
        val tagValue = obj.tags[key]?.toCheckDate() ?: return false
        return compareTo(tagValue)
    }
}

class TagOlderThan(key: String, dateFilter: DateFilter) : CompareTagAge(key, dateFilter) {
    override fun toString() = "$key older $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue < dateFilter.date
}
class TagNewerThan(key: String, dateFilter: DateFilter) : CompareTagAge(key, dateFilter) {
    override fun toString() = "$key newer $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue > dateFilter.date
}

abstract class CompareTagAge(val key: String, val dateFilter: DateFilter) : ElementFilter {
    abstract fun compareTo(tagValue: LocalDate): Boolean
    override fun matches(obj: Element): Boolean {
        if (compareTo(Instant.fromEpochMilliseconds(obj.timestampEdited).toLocalDate())) return true
        return getLastCheckDateKeys(key)
            .mapNotNull { obj.tags[it]?.toCheckDate() }
            .any { compareTo(it) }
    }
}

class ElementOlderThan(dateFilter: DateFilter) : CompareElementAge(dateFilter) {
    override fun toString() = "older $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue < dateFilter.date
}
class ElementNewerThan(dateFilter: DateFilter) : CompareElementAge(dateFilter) {
    override fun toString() = "newer $dateFilter"
    override fun compareTo(tagValue: LocalDate) = tagValue > dateFilter.date
}

abstract class CompareElementAge(val dateFilter: DateFilter) : ElementFilter {
    abstract fun compareTo(tagValue: LocalDate): Boolean
    override fun matches(obj: Element) = compareTo(Instant.fromEpochMilliseconds(obj.timestampEdited).toLocalDate())
}

class CombineFilters(vararg val filters: ElementFilter) : ElementFilter {
    override fun toString() = filters.joinToString(" and ")
    override fun matches(obj: Element) = filters.all { it.matches(obj) }
}
