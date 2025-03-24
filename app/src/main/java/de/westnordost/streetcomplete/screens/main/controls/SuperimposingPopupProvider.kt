package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Places the popup on top of the anchor, superimposing it. I.e. not like a tooltip next to the
 * anchor, but on top of it, covering it. It then extends from the calculated alignment of the
 * anchor in relation to the window (like tooltips). For example, if the anchor is aligned to the
 * bottom-right, the popup extends towards the upper-left.
 *
 * @param onAlignment callback to report how the popup has been aligned
 */
@Immutable
internal class SuperimposingPopupPositionProvider(
    private val onAlignment: (alignLeft: Boolean, alignTop: Boolean) -> Unit = { _, _ -> }
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val alignLeft = anchorBounds.left <= windowSize.width - anchorBounds.right
        val alignTop = anchorBounds.top <= windowSize.height - anchorBounds.bottom
        onAlignment(alignLeft, alignTop)

        return IntOffset(
            x = if (alignLeft) anchorBounds.left else anchorBounds.right - popupContentSize.width,
            y = if (alignTop) anchorBounds.top else anchorBounds.bottom - popupContentSize.height,
        )
    }
}
