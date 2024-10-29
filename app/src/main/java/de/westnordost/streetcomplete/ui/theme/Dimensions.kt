package de.westnordost.streetcomplete.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun getMaxQuestFormWidth(totalWidth: Dp): Dp =
    if (totalWidth >= 820.dp) 480.dp
    else if (totalWidth >= 600.dp) 360.dp
    else 480.dp

fun getQuestFormPeekHeight(isLandscape: Boolean): Dp =
    if (isLandscape) 442.dp
    else 352.dp
