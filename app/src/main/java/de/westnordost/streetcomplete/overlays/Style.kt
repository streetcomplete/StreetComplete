package de.westnordost.streetcomplete.overlays

sealed interface Style

data class PolylineStyle(
    /** argb (center) line color. null if no center line should be drawn */
    val color: String?,
    /** argb left line color. null if no left line should be drawn */
    val colorLeft: String? = null,
    /** argb right line color. null if no right line should be drawn */
    val colorRight: String? = null,
    /** label to show on the line (centered) */
    val label: String? = null,
) : Style

data class PolygonStyle(
    /** argb value as hex value, e.g. "#66ff00" */
    val color: String,
    /** label to show in the center of the area */
    val label: String? = null,
) : Style

data class PointStyle(
    /** label to show on the point */
    val label: String?
) : Style
