package de.westnordost.streetcomplete.layers

sealed interface Style

data class LineStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** side on which to draw. Null if no side (but center) */
    val side: Side? = null,
    /** label to show on the line */
    val label: String?,
    // TODO (maybe enable dashed/dotted lines later)
) : Style {
    enum class Side { LEFT, RIGHT }
}

data class AreaStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** argb value as hex value, e.g. "#66ff00" */
    val strokeColor: String?,
    /** label to show in the center of the area */
    val label: String?,
    // TODO (maybe enable the possibility to add icons later)
) : Style

data class PointStyle(
    /** label to show on the point */
    val label: String?
) : Style
