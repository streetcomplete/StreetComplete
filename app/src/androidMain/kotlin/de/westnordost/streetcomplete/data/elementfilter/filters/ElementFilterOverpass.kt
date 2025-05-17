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
        "[%s](if: date(t[${key.quote()}]) $operator date('$strVal'))".formatQuoted(key)
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
        "[%s](if: number(t[${key.quote()}]) $operator $strVal)".formatQuoted(key)
    }
    is HasKey -> "[%s]".formatQuoted(key)
    is HasKeyLike -> "[~%s ~ '.*']".formatQuoted("^($key)$")
    is HasTag -> "[%s = %s]".formatQuoted(key, value)
    is HasTagLike -> "[~%s ~ %s]".formatQuoted("^($key)$", "^($value)$")
    is NotHasTagLike -> "[~%s !~ %s]".formatQuoted("^($key)$", "^($value)$")
    is HasTagValueLike -> "[%s ~ %s]".formatQuoted(key, "^($value)$")
    is NotHasKey -> "[!%s]".formatQuoted(key)
    is NotHasKeyLike -> "[!~%s ~ '.*']".formatQuoted("^($key)$")
    is NotHasTag -> "[%s != %s]".formatQuoted(key, value)
    is NotHasTagValueLike -> "[%s !~ %s]".formatQuoted(key, "^($value)$")
}

private fun String.formatQuoted(vararg args: Any?): String =
    format(*args.map { it.toString().quoteIfNecessary() }.toTypedArray())

private val QUOTES_NOT_REQUIRED = Regex("[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+")

private fun String.quoteIfNecessary() =
    if (QUOTES_NOT_REQUIRED.matches(this)) this else quote()

private fun String.quote() = "'${replace("'", "\'")}'"
