package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection.Ltr

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues.Absolute(
    left = calculateLeftPadding(Ltr) + other.calculateLeftPadding(Ltr),
    top = calculateTopPadding() + other.calculateTopPadding(),
    right = calculateRightPadding(Ltr) + other.calculateRightPadding(Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding(),
)
