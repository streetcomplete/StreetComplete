package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.isLandscape

object Dimensions {

    val speechBubbleCornerRadius: Dp get() = 16.dp

    /** Collapsed quest form height as a fraction of the window, so list answers start well above
     *  the bottom of the screen on both compact and tall devices. */
    fun getQuestFormPeekHeight(windowInfo: WindowInfo): Dp =
        (windowInfo.containerDpSize.height * 0.6f).coerceAtLeast(360.dp)

    fun getMaxQuestFormWidth(windowInfo: WindowInfo): Dp =
        if (windowInfo.isLandscape) {
            // in landscape mode, the quest form is by default expanded, so it already takes up
            // more/all vertical space. Especially on smaller screens, the width thus must be
            // somewhat limited so that the map is still visible
            if (windowInfo.containerDpSize.width > 820.dp) 480.dp
            else 360.dp
        } else {
            // in portrait mode, it may stretch very wide
            480.dp
        }

    /** Padding on the map due to an open quest form */
    fun getOpenQuestFormMapPadding(windowInfo: WindowInfo, windowInsets: PaddingValues): PaddingValues {
        val isLandscape = windowInfo.isLandscape
        val buttons = 56.dp // also consider the top with the star counter, menu button as obscured
        val insetsTop = windowInsets.calculateTopPadding()
        val insetsBottom = windowInsets.calculateBottomPadding()
        val insetsLeft = windowInsets.calculateLeftPadding(LayoutDirection.Ltr)
        val insetsRight = windowInsets.calculateRightPadding(LayoutDirection.Ltr)

        return if (isLandscape) {
            PaddingValues.Absolute(
                left = getMaxQuestFormWidth(windowInfo) - insetsRight,
                top = insetsTop + buttons,
                right = insetsRight,
                bottom = insetsBottom + buttons,
            )
        } else {
            PaddingValues.Absolute(
                left = insetsLeft,
                top = insetsTop + buttons,
                right = insetsRight,
                bottom = getQuestFormPeekHeight(windowInfo) - insetsTop
            )
        }
    }
}
