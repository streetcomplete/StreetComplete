package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.meta.*
import java.util.*

interface ElementFilter : Matcher<Element> {
    fun toOverpassQLString(): String
}

/** key */
class HasKey(val key: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + key.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.containsKey(key) ?: false
}

/** !key */
class NotHasKey(val key: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + "!" + key.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = !(obj?.tags?.containsKey(key) ?: true)
}

/** key = value */
class HasTag(val key: String, val value: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + key.quoteIfNecessary() + " = " + value.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key) == value
}

/** ~key(word)? */
class HasKeyLike(key: String): ElementFilter {
    val key = key.toRegex()

    override fun toOverpassQLString() = "[" + "~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ '.*']"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { it.matches(key) } != null
}

/** key != value */
class NotHasTag(val key: String, val value: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + key.quoteIfNecessary() + " != " + value.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key) != value
}

/** key ~ val(ue)? */
class HasTagValueLike(val key: String, value: String) : ElementFilter {
    val value = value.toRegex()

    override fun toOverpassQLString() =
        "[" + key.quoteIfNecessary() + " ~ " + "^(${value.pattern})$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key)?.matches(value) ?: false
}

/** key !~ val(ue)? */
class NotHasTagValueLike(val key: String, value: String) : ElementFilter {
    val value = value.toRegex()

    override fun toOverpassQLString() =
        "[" + key.quoteIfNecessary() + " !~ " + "^(${value.pattern})$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = !(obj?.tags?.get(key)?.matches(value) ?: false)
}

/** ~key(word)? ~ val(ue)? */
class HasTagLike(key: String, value: String) : ElementFilter {
    val key = key.toRegex()
    val value = value.toRegex()

    override fun toOverpassQLString() = 
        "[" + "~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ " + "^(${value.pattern})$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?) =
        obj?.tags?.entries?.find { it.key.matches(key) && it.value.matches(value) } != null
}

sealed class CompareTagValue(val key: String, val value: Float): ElementFilter {
    override fun toOverpassQLString() : String {
        val strVal = if (value - value.toInt() == 0f) value.toInt().toString() else value.toString()
        return "[" + key.quoteIfNecessary() + "](if: number(t[" + key.quote() + "]) " + operator + " " + strVal + ")"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val tagValue = obj?.tags?.get(key)?.toFloatOrNull() ?: return false
        return compareTo(tagValue)
    }

    abstract fun compareTo(tagValue: Float): Boolean
    abstract val operator: String
}

/** key > value */
class HasTagGreaterThan(key: String, value: Float): CompareTagValue(key, value) {
    override val operator = ">"
    override fun compareTo(tagValue: Float) = tagValue > value
}

/** key >= value */
class HasTagGreaterOrEqualThan(key: String, value: Float): CompareTagValue(key, value) {
    override val operator = ">="
    override fun compareTo(tagValue: Float) = tagValue >= value
}

/** key < value */
class HasTagLessThan(key: String, value: Float): CompareTagValue(key, value) {
    override val operator = "<"
    override fun compareTo(tagValue: Float) = tagValue < value
}

/** key <= value */
class HasTagLessOrEqualThan(key: String, value: Float): CompareTagValue(key, value) {
    override val operator = "<="
    override fun compareTo(tagValue: Float) = tagValue <= value
}

sealed class CompareDateTagValue(val key: String, val date: Date): ElementFilter {
    override fun toOverpassQLString() : String {
        val strVal = date.toCheckDateString()
        return "[" + key.quoteIfNecessary() + "](if: date(t[" + key.quote() + "]) " + operator + " date('" + strVal + "'))"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val tagValue = obj?.tags?.get(key)?.toCheckDate() ?: return false
        return compareTo(tagValue)
    }

    abstract fun compareTo(tagValue: Date): Boolean
    abstract val operator: String
}

/** key > date */
class HasDateTagGreaterThan(key: String, date: Date): CompareDateTagValue(key, date) {
    override val operator = ">"
    override fun compareTo(tagValue: Date) = tagValue > date
}

/** key >= date */
class HasDateTagGreaterOrEqualThan(key: String, date: Date): CompareDateTagValue(key, date) {
    override val operator = ">="
    override fun compareTo(tagValue: Date) = tagValue >= date
}

/** key < date */
class HasDateTagLessThan(key: String, date: Date): CompareDateTagValue(key, date) {
    override val operator = "<"
    override fun compareTo(tagValue: Date) = tagValue < date
}

/** key <= date */
class HasDateTagLessOrEqualThan(key: String, date: Date): CompareDateTagValue(key, date) {
    override val operator = "<="
    override fun compareTo(tagValue: Date) = tagValue <= date
}

/** key older 4 years */
class TagOlderThan(val key: String, val daysAgo: Float) : ElementFilter {

    override fun toOverpassQLString(): String {
        val date = dateDaysAgo(daysAgo).toCheckDateString()
        val datesToCheck = (listOf("timestamp()") + getLastCheckDateKeys(key).map { "t['$it']" })
        return "[" + key.quoteIfNecessary() + "](if: " + datesToCheck.joinToString(" || ") { "date($it) < date('$date')" } + ")"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val dateElementEdited = obj?.dateEdited ?: return false

        if (!obj.tags.containsKey(key)) return false

        val date = dateDaysAgo(daysAgo)

        if (dateElementEdited < date) return true

        return getLastCheckDateKeys(key)
            .mapNotNull { obj.tags[it]?.toCheckDate() }
            .any { it < date }
    }
}

/** older 4 years */
class ElementOlderThan(val daysAgo: Float) : ElementFilter {
    override fun toOverpassQLString(): String {
        val date = dateDaysAgo(daysAgo).toCheckDateString()
        return "(if: date(timestamp()) < date('$date'))"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val dateElementEdited = obj?.dateEdited ?: return false
        val date = dateDaysAgo(daysAgo)
        return dateElementEdited < date
    }
}

private val QUOTES_NOT_REQUIRED = "[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+".toRegex()

private fun String.quoteIfNecessary() =
    if (QUOTES_NOT_REQUIRED.matches(this)) this else quote()

private fun String.quote() = "'${this.replace("'", "\'")}'"