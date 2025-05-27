package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.osm.getLastCheckDateKeys
import de.westnordost.streetcomplete.osm.toCheckDateString

fun ElementFilter.toOverpassString(): String = when (this) {
    is CombineFilters -> filters.joinToString("") { it.toOverpassString() }
    is CompareDateTagValue -> {
        val strVal = dateFilter.date.toCheckDateString()
        val operator = when (this) {
            is HasDateTagGreaterOrEqualThan -> ">="
            is HasDateTagLessOrEqualThan -> "<="
            is HasDateTagGreaterThan -> ">"
            is HasDateTagLessThan -> "<"
            else -> throw UnsupportedOperationException()
        }
        "[${key.quoteIfNecessary()}](if: date(t[${key.quote()}]) $operator date('$strVal'))"
    }
    is CompareElementAge -> {
        val dateStr = dateFilter.date.toCheckDateString()
        val operator = when (this) {
            is ElementNewerThan -> ">"
            is ElementOlderThan -> "<"
            else -> throw UnsupportedOperationException()
        }
        "(if: date(timestamp()) $operator date('$dateStr'))"
    }
    is CompareTagAge -> {
        val dateStr = dateFilter.date.toCheckDateString()
        val datesToCheck = (listOf("timestamp()") + getLastCheckDateKeys(key).map { "t['$it']" })
        val operator = when (this) {
            is TagNewerThan -> ">"
            is TagOlderThan -> "<"
            else -> throw UnsupportedOperationException()
        }
        val oqlEvaluators = datesToCheck.joinToString(" || ") { "date($it) $operator date('$dateStr')" }
        "(if: $oqlEvaluators)"
    }
    is CompareTagValue -> {
        val strVal = if (value - value.toInt() == 0f) value.toInt().toString() else value.toString()
        val operator = when (this) {
            is HasTagGreaterOrEqualThan -> ">="
            is HasTagLessOrEqualThan -> "<="
            is HasTagGreaterThan -> ">"
            is HasTagLessThan -> "<"
            else -> throw UnsupportedOperationException()
        }
        "[${key.quoteIfNecessary()}](if: number(t[${key.quote()}]) $operator $strVal)"
    }
    is HasKey -> "[${key.quoteIfNecessary()}]"
    is HasKeyLike -> "[~${key.toPattern().quote()} ~ '.*']"
    is HasTag -> "[${key.quoteIfNecessary()} = ${value.quoteIfNecessary()}]"
    is HasTagLike -> "[~${key.toPattern().quote()} ~ ${value.toPattern().quote()}]"
    is NotHasTagLike -> "[~${key.toPattern().quote()} !~ ${value.toPattern().quote()}]"
    is HasTagValueLike -> "[${key.quoteIfNecessary()} ~ ${value.toPattern().quote()}]"
    is NotHasKey -> "[!${key.quoteIfNecessary()}]"
    is NotHasKeyLike -> "[!~${key.toPattern().quote()} ~ '.*']"
    is NotHasTag -> "[${key.quoteIfNecessary()} != ${value.quoteIfNecessary()}]"
    is NotHasTagValueLike -> "[${key.quoteIfNecessary()} !~ ${value.toPattern().quote()}]"
}

private fun String.toPattern(): String = "^($this)$"

private val QUOTES_NOT_REQUIRED = Regex("[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+")

private fun String.quoteIfNecessary() =
    if (QUOTES_NOT_REQUIRED.matches(this)) this else quote()

private fun String.quote() = "'${replace("'", "\'")}'"
