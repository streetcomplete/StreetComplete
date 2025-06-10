package de.westnordost.streetcomplete.data.overlays

import androidx.compose.ui.graphics.Color

sealed interface OverlayStyle {
    data class Polyline(
        /** center line style. null if no center line should be drawn */
        val stroke: Stroke?,
        /** left line style. null if no left line should be drawn */
        val strokeLeft: Stroke? = null,
        /** argb right line color. null if no right line should be drawn */
        val strokeRight: Stroke? = null,
        /** label to show on the line (centered) */
        val label: String? = null,
    ) : OverlayStyle

    data class Stroke(
        /** stroke color */
        val color: Color,
        /** whether the line is dashed */
        val dashed: Boolean = false,
    )

    data class Polygon(
        /** polygon area color */
        val color: Color,
        /** icon id to show on the point */
        val icon: Int? = null,
        /** label to show in the center of the area */
        val label: String? = null,
        /** whether and how much to extrude this area */
        val height: Float? = null,
        val minHeight: Float? = null,
    ) : OverlayStyle

    data class Point(
        /** icon id to show on the point */
        val icon: Int?,
        /** label to show on the point */
        val label: String? = null,
    ) : OverlayStyle

}

