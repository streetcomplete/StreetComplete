package de.westnordost.streetcomplete.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Put the popup in some screen corner */
class ScreenAlignmentPopupPositionProvider(
    val alignment: Alignment,
    val windowInsets: WindowInsets,
    val density: Density,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val padLeft = windowInsets.getLeft(density, layoutDirection)
        val padTop = windowInsets.getTop(density)
        /* for some reason, windowSize seems to already take into account *some* window insets,
         * i.e. windowSize is smaller than the actual window because the insets have already been
         * subtracted. While for the return value, an offset relative to the *full* window size
         * is expected. Hence, the added offset here in the return statement.
         * (It looks like a bug in Compose, but maybe it is a configuration thing with edge-to-edge)
         *  */
        val point = alignment.align(popupContentSize, windowSize, layoutDirection)
        return point + IntOffset(padLeft, padTop)
    }
}

@Composable
fun rememberScreenAlignmentPopupPositionProvider(
    alignment: Alignment
): ScreenAlignmentPopupPositionProvider {
    val windowInsets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    return remember(windowInsets, density) {
        ScreenAlignmentPopupPositionProvider(
            alignment = alignment,
            windowInsets = windowInsets,
            density = density,
        )
    }
}
