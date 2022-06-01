package de.westnordost.streetcomplete.osm.opening_hours.model

fun months(bits12: Int) = Months(bits12.toBitField(12))

fun weekdays(bits8: Int) = Weekdays(bits8.toBitField(8))

private fun Int.toBitField(bits: Int) = (bits - 1 downTo 0).map { this and (1 shl it) != 0 }.toBooleanArray()
