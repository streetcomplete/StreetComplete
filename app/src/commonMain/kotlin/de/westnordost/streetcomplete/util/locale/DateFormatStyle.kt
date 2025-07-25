package de.westnordost.streetcomplete.util.locale

/** Local date-time format style */
enum class DateFormatStyle {
    /** e.g. "7/24/25, 12:34 PM" */
    Short,
    /** e.g. "Jul 24, 2025, 12:34:56 PM" */
    Medium,
    /** e.g. "July 24, 2025, 12:34:56 PM UTC" */
    Long,
    /** e.g. "Thursday, July 24, 2025, 12:34:56 PM Coordinated Universal Time" */
    Full
}
