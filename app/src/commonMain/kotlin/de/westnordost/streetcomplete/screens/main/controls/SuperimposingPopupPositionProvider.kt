package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.absoluteValue

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
    private val onAlignment: (alignLeft: Boolean, alignTop: Boolean, popupWidth: Int) -> Unit = { _, _, _ -> }
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val alignLeft = anchorBounds.left <= windowSize.width - anchorBounds.right
        val alignTop = anchorBounds.top <= windowSize.height - anchorBounds.bottom

        val position =
            IntOffset(
                x = if (alignLeft) anchorBounds.left else anchorBounds.right - popupContentSize.width,
                y = if (alignTop) anchorBounds.top else anchorBounds.bottom - popupContentSize.height,
            )

        val overflow =
            if (alignLeft) (position.x + popupContentSize.width - windowSize.width).coerceAtLeast(0)
            else position.x.coerceAtMost(0).absoluteValue

        val popupWidth = popupContentSize.width - overflow
        onAlignment(alignLeft, alignTop, popupWidth)
        return position
    }
}
