package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Padding on the map due to an open quest form */
fun getOpenQuestFormMapPadding(totalWidth: Dp, totalHeight: Dp, ): PaddingValues {
    val isLandscape = totalWidth > totalHeight
    return PaddingValues.Absolute(
        left = if (isLandscape) getMaxQuestFormWidth(totalWidth) else 0.dp,
        top = 0.dp,
        right = 0.dp,
        bottom = if (isLandscape) 0.dp else 320.dp
    )
}

fun getMaxQuestFormWidth(totalWidth: Dp): Dp =
    if (totalWidth >= 820.dp) 480.dp
    else if (totalWidth >= 600.dp) 360.dp
    else 480.dp

fun getQuestFormPeekHeight(isLandscape: Boolean): Dp =
    if (isLandscape) 540.dp
    else 400.dp
