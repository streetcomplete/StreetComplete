package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // theme is more speech-bubbly than default
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp)
)

val Shapes.speechBubbleCornerRadius: Dp get() = 16.dp
