package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_keyboard_24
import org.jetbrains.compose.resources.painterResource

/** Popup button to switch keyboard between ABC and 123, displayed in some corner of the screen.
 *
 *  Only works correctly in edge-to-edge. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchKeyboardPopupButton(
    isAbc: Boolean = false,
    onChange: (isAbc: Boolean) -> Unit,
    alignment: Alignment = Alignment.BottomStart,
) {
    Popup(ScreenAlignmentPopupPositionProvider(alignment)) {
        Surface(
            onClick = { onChange(!isAbc) },
            modifier = Modifier.size(64.dp).padding(8.dp),
            color = Color.Black,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
            ) {
                Text(
                    // shows what it changes to, not what it currently is, just like other mode
                    // change buttons on the keyboard (e.g. "?123")
                    text = if (isAbc) "123" else "ABC", letterSpacing = 0.sp,
                    style = MaterialTheme.typography.button
                )
                Icon(painterResource(Res.drawable.ic_keyboard_24), null)
            }
        }
    }
}

private class ScreenAlignmentPopupPositionProvider(val alignment: Alignment) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val point = alignment.align(popupContentSize, windowSize, layoutDirection)
        return point
    }
}
