package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.RectF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a group of days with the same opening hours.
 * E.g., "Mo-Fr" for Monday through Friday, or "Sa" for Saturday only.
 */
@Parcelize
data class DayGroup(
    val days: List<Weekday>,
) : Parcelable {
    /** Returns the OSM-formatted day range string (e.g., "Mo-Fr", "Sa", "Mo,We,Fr") */
    fun toOsmDayString(): String {
        if (days.isEmpty()) return ""
        if (days.size == 1) return days.first().osmAbbrev

        // Check if days are consecutive
        val sorted = days.sortedBy { it.ordinal }
        val isConsecutive = sorted.zipWithNext().all { (a, b) -> b.ordinal - a.ordinal == 1 }

        return if (isConsecutive && sorted.size > 1) {
            "${sorted.first().osmAbbrev}-${sorted.last().osmAbbrev}"
        } else {
            sorted.joinToString(",") { it.osmAbbrev }
        }
    }

    /** Display name for UI (e.g., "Mon-Fri", "Sat") */
    fun toDisplayString(): String {
        if (days.isEmpty()) return ""
        if (days.size == 1) return days.first().displayName

        val sorted = days.sortedBy { it.ordinal }
        val isConsecutive = sorted.zipWithNext().all { (a, b) -> b.ordinal - a.ordinal == 1 }

        return if (isConsecutive && sorted.size > 1) {
            "${sorted.first().displayName}-${sorted.last().displayName}"
        } else {
            sorted.joinToString(", ") { it.displayName }
        }
    }

    companion object {
        /** All days of the week as a single group */
        val ALL_DAYS = DayGroup(Weekday.entries.toList())

        /** Weekdays (Monday-Friday) */
        val WEEKDAYS = DayGroup(listOf(Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY))

        /** Weekend (Saturday-Sunday) */
        val WEEKEND = DayGroup(listOf(Weekday.SATURDAY, Weekday.SUNDAY))

        /** Saturday only */
        val SATURDAY = DayGroup(listOf(Weekday.SATURDAY))

        /** Sunday only */
        val SUNDAY = DayGroup(listOf(Weekday.SUNDAY))
    }
}

/**
 * Days of the week with OSM abbreviations.
 */
enum class Weekday(val osmAbbrev: String, val displayName: String) {
    MONDAY("Mo", "Mon"),
    TUESDAY("Tu", "Tue"),
    WEDNESDAY("We", "Wed"),
    THURSDAY("Th", "Thu"),
    FRIDAY("Fr", "Fri"),
    SATURDAY("Sa", "Sat"),
    SUNDAY("Su", "Sun");
}

/**
 * Preset options for day grouping selection.
 */
enum class DayGroupingPreset {
    /** Same hours every day (Mo-Su) */
    SAME_ALL_DAYS,
    /** Weekdays vs weekend (Mo-Fr, Sa-Su) */
    WEEKDAYS_WEEKEND,
    /** Weekdays, Saturday, Sunday (Mo-Fr, Sa, Su) */
    WEEKDAYS_SAT_SUN,
    /** Custom grouping */
    CUSTOM;

    fun toGroups(): List<DayGroup> = when (this) {
        SAME_ALL_DAYS -> listOf(DayGroup.ALL_DAYS)
        WEEKDAYS_WEEKEND -> listOf(DayGroup.WEEKDAYS, DayGroup.WEEKEND)
        WEEKDAYS_SAT_SUN -> listOf(DayGroup.WEEKDAYS, DayGroup.SATURDAY, DayGroup.SUNDAY)
        CUSTOM -> emptyList() // Custom groups are built by user
    }
}

/**
 * Annotation state for a single day group during photo annotation.
 * Captures the bounding boxes drawn by the user for open and close times.
 */
@Parcelize
data class DayAnnotation(
    val dayGroup: DayGroup,
    /** Bounding box for the highlighted open time region (green) */
    val openRegion: RectF? = null,
    /** Bounding box for the highlighted close time region (red) */
    val closeRegion: RectF? = null,
    /** Raw OCR result for open time (numbers only) */
    val openTimeRaw: String? = null,
    /** Raw OCR result for close time (numbers only) */
    val closeTimeRaw: String? = null,
    /** Whether this day group is marked as closed (no hours) */
    val isClosed: Boolean = false,
) : Parcelable

/**
 * Verified and edited hours for a day group, ready for OSM submission.
 */
@Parcelize
data class VerifiedHours(
    val dayGroup: DayGroup,
    val openHour: Int,
    val openMinute: Int,
    val closeHour: Int,
    val closeMinute: Int,
    val isClosed: Boolean = false,
) : Parcelable {
    /**
     * Converts to OSM opening_hours format string for this day group.
     * E.g., "Mo-Fr 08:00-17:00" or "Sa off"
     */
    fun toOsmString(): String {
        val days = dayGroup.toOsmDayString()
        return if (isClosed) {
            "$days off"
        } else {
            val open = "${openHour.toString().padStart(2, '0')}:${openMinute.toString().padStart(2, '0')}"
            val close = "${closeHour.toString().padStart(2, '0')}:${closeMinute.toString().padStart(2, '0')}"
            "$days $open-$close"
        }
    }
}

/**
 * Complete result from the OCR flow, containing all verified hours.
 */
@Parcelize
data class OcrOpeningHoursResult(
    val hours: List<VerifiedHours>
) : Parcelable {
    /**
     * Converts to complete OSM opening_hours tag value.
     * E.g., "Mo-Fr 08:00-17:00; Sa 09:00-12:00; Su off"
     */
    fun toOsmOpeningHours(): String {
        return hours
            .filter { !it.isClosed || it.dayGroup.days.isNotEmpty() }
            .joinToString("; ") { it.toOsmString() }
    }
}

/**
 * State for the overall OCR flow.
 */
@Parcelize
data class OcrFlowState(
    /** Selected day grouping preset */
    val groupingPreset: DayGroupingPreset = DayGroupingPreset.SAME_ALL_DAYS,
    /** Day groups to annotate (either from preset or custom) */
    val dayGroups: List<DayGroup> = DayGroupingPreset.SAME_ALL_DAYS.toGroups(),
    /** Current annotations for each day group */
    val annotations: List<DayAnnotation> = emptyList(),
    /** Index of the currently active day group being annotated */
    val currentGroupIndex: Int = 0,
    /** Whether we're in 12-hour mode (true) or 24-hour mode (false) */
    val is12HourMode: Boolean = true,
    /** Verified hours after OCR and user editing */
    val verifiedHours: List<VerifiedHours> = emptyList(),
) : Parcelable {
    val currentDayGroup: DayGroup?
        get() = dayGroups.getOrNull(currentGroupIndex)

    val isLastGroup: Boolean
        get() = currentGroupIndex >= dayGroups.size - 1

    val progress: String
        get() = "${currentGroupIndex + 1} of ${dayGroups.size}"
}

/**
 * Extension function to convert OCR results to the format used by OpeningHoursAdapter.
 */
fun OcrOpeningHoursResult.toOpeningHoursRows(): List<de.westnordost.streetcomplete.osm.opening_hours.model.OpeningHoursRow> {
    val rows = mutableListOf<de.westnordost.streetcomplete.osm.opening_hours.model.OpeningHoursRow>()

    for (verifiedHours in hours) {
        if (verifiedHours.isClosed) {
            // Create OffDaysRow for closed days
            val weekdays = verifiedHours.dayGroup.toWeekdays()
            rows.add(de.westnordost.streetcomplete.osm.opening_hours.model.OffDaysRow(weekdays))
        } else {
            // Create OpeningWeekdaysRow for open days
            val weekdays = verifiedHours.dayGroup.toWeekdays()
            val timeRange = de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange(
                start = verifiedHours.openHour * 60 + verifiedHours.openMinute,
                end = verifiedHours.closeHour * 60 + verifiedHours.closeMinute
            )
            rows.add(de.westnordost.streetcomplete.osm.opening_hours.model.OpeningWeekdaysRow(weekdays, timeRange))
        }
    }

    return rows
}

/**
 * Converts a DayGroup to the Weekdays format used by the opening hours model.
 */
fun DayGroup.toWeekdays(): de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays {
    val selection = BooleanArray(8) // 7 days + PH (public holiday)
    for (day in days) {
        selection[day.ordinal] = true
    }
    return de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays(selection)
}
