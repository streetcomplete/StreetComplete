package de.westnordost.streetcomplete.overlays

sealed interface Style

data class PolylineStyle(
    /** center line style. null if no center line should be drawn */
    val stroke: StrokeStyle?,
    /** left line style. null if no left line should be drawn */
    val strokeLeft: StrokeStyle? = null,
    /** argb right line color. null if no right line should be drawn */
    val strokeRight: StrokeStyle? = null,
    /** label to show on the line (centered) */
    val label: String? = null,
) : Style

data class StrokeStyle(
    /** argb line color */
    val color: String,
    /** whether the line is dashed */
    val dashed: Boolean = false,
)

data class PolygonStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** icon name to show on the point */
    val icon: String? = null,
    /** label to show in the center of the area */
    val label: String? = null,
) : Style

data class PointStyle(
    /** icon name to show on the point */
    val icon: String?,
    /** label to show on the point */
    val label: String? = null,
) : Style
