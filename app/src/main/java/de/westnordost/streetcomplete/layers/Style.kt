package de.westnordost.streetcomplete.layers

sealed interface Style

data class PolylineStyle(
    /** (center) line style. null if no center line should be drawn */
    val stroke: StrokeStyle?,
    /** left line style. null if no left line should be drawn */
    val strokeLeft: StrokeStyle? = null,
    /** right line style. null if no right line should be drawn */
    val strokeRight: StrokeStyle? = null,
    /** label to show on the line (centered) */
    val label: String? = null,
) : Style

data class PolygonStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** label to show in the center of the area */
    val label: String?,
    // TODO LAYERS (maybe enable the possibility to add icons later)
) : Style

data class PointStyle(
    /** label to show on the point */
    val label: String?
) : Style

data class StrokeStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    // TODO LAYERS (maybe enable dashed/dotted lines later)
)
