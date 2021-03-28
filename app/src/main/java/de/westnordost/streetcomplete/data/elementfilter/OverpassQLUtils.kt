package de.westnordost.streetcomplete.data.elementfilter

private val QUOTES_NOT_REQUIRED = Regex("[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+")

fun String.quoteIfNecessary() =
    if (QUOTES_NOT_REQUIRED.matches(this)) this else quote()

fun String.quote() = "'${this.replace("'", "\'")}'"
