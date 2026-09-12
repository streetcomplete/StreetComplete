package de.westnordost.streetcomplete.quests.socket

/**
 * Surveyed state of one `socket:<type>` key.
 * Distinguishes missing, unresolved `yes`, explicit `no`, and a positive count.
 */
sealed interface SocketPresence {
    /** Key missing or unrecognized — not surveyed. */
    data object Unspecified : SocketPresence

    /** Present according to OSM (`yes`) but count unknown. */
    data object Yes : SocketPresence

    /** Explicitly absent (`no` or count 0). */
    data object Absent : SocketPresence

    /** Positive surveyed count. */
    data class Count(val value: Int) : SocketPresence {
        init { require(value > 0) }
    }
}

fun parseSocketPresence(value: String?): SocketPresence = when {
    value == null -> SocketPresence.Unspecified
    value == "yes" -> SocketPresence.Yes
    value == "no" -> SocketPresence.Absent
    else -> value.toIntOrNull()?.let { count ->
        if (count > 0) SocketPresence.Count(count) else SocketPresence.Absent
    } ?: SocketPresence.Unspecified
}

/** Form field: null = unresolved (missing/`yes`); 0 = explicit no; >0 = count. */
fun SocketPresence.toFormCount(): Int? = when (this) {
    SocketPresence.Unspecified, SocketPresence.Yes -> null
    SocketPresence.Absent -> 0
    is SocketPresence.Count -> value
}

fun isExplicitlySurveyed(presence: SocketPresence): Boolean =
    presence is SocketPresence.Absent || presence is SocketPresence.Count

/**
 * A displayed-type survey is complete only when every shown type is numeric or explicit `no`.
 * Missing keys and `yes` are incomplete.
 */
fun isCompleteSocketSurvey(
    tags: Map<String, String>,
    displayedTypes: List<SocketType>,
): Boolean =
    displayedTypes.isNotEmpty() &&
        displayedTypes.all { isExplicitlySurveyed(parseSocketPresence(tags[it.osmCountKey])) }

fun initialSocketFormCounts(
    tags: Map<String, String>,
    socketTypes: List<SocketType>,
): Map<SocketType, Int?> =
    socketTypes.associateWith { parseSocketPresence(tags[it.osmCountKey]).toFormCount() }
