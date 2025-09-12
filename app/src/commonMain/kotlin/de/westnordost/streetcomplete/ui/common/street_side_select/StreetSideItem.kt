package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.ui.graphics.painter.Painter

data class StreetSideItem(
    val image: Painter?,
    val title: String?,
    val floatingIcon: Painter? = null
)
