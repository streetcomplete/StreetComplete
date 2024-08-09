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
    /** rgb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** icon id to show on the point */
    val icon: Int? = null,
    /** label to show in the center of the area */
    val label: String? = null,
    /** whether and how much to extrude this area */
    val height: Float? = null,
    val minHeight: Float? = null,
) : Style

data class PointStyle(
    /** icon id to show on the point */
    val icon: Int?,
    /** label to show on the point */
    val label: String? = null,
) : Style
