package de.westnordost.streetcomplete.util.ktx

typealias Instant = java.time.Instant

typealias LocalDate = java.time.LocalDate

typealias LocalDateTime = java.time.LocalDateTime

typealias LocalTime = java.time.LocalTime

typealias Month = java.time.Month

typealias ZoneId = java.time.ZoneId

typealias ZoneOffset = java.time.ZoneOffset

fun LocalDate.toInstant(): Instant =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant()

fun LocalDate.toEpochMilli(): Long = this.toInstant().toEpochMilli()

fun Instant.toLocalDate(): LocalDate =
    this.atZone(ZoneId.systemDefault()).toLocalDate()

fun isApril1st(): Boolean {
    val now = LocalDate.now()
    return now.dayOfMonth == 1 && now.month == Month.APRIL
}
