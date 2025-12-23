package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues.Absolute(
    left = calculateLeftPadding(Ltr) + other.calculateLeftPadding(Ltr),
    top = calculateTopPadding() + other.calculateTopPadding(),
    right = calculateRightPadding(Ltr) + other.calculateRightPadding(Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding(),
)

operator fun PaddingValues.minus(other: PaddingValues): PaddingValues = PaddingValues.Absolute(
    left = max(calculateLeftPadding(Ltr) - other.calculateLeftPadding(Ltr), 0.dp),
    top = max(calculateTopPadding() - other.calculateTopPadding(), 0.dp),
    right = max(calculateRightPadding(Ltr) - other.calculateRightPadding(Ltr), 0.dp),
    bottom = max(calculateBottomPadding() - other.calculateBottomPadding(), 0.dp),
)
