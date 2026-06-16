package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Shapes
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.isLandscape

object Dimensions {

    val speechBubbleCornerRadius: Dp get() = 16.dp

    val QuestFormPeekHeight = 400.dp

    fun getMaxQuestFormWidth(windowInfo: WindowInfo): Dp =
        if (windowInfo.isLandscape) {
            // in landscape mode, the quest form is by default expanded, so it already takes up
            // more/all vertical space. Especially on smaller screens, the width thus must be
            // somewhat limited so that the map is still visible
            if (windowInfo.containerDpSize.width > 820.dp) 480.dp
            else 360.dp
        }
        else {
            // in portrait mode, it may stretch very wide
            480.dp
        }

    /** Padding on the map due to an open quest form */
    fun getOpenQuestFormMapPadding(windowInfo: WindowInfo): PaddingValues {
        val isLandscape = windowInfo.isLandscape
        return PaddingValues.Absolute(
            left = if (isLandscape) getMaxQuestFormWidth(windowInfo) else 0.dp,
            top = 0.dp,
            right = 0.dp,
            bottom = if (isLandscape) 0.dp else 320.dp
        )
    }
}
