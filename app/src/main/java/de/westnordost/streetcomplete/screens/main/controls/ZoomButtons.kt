package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.ZoomInIcon
import de.westnordost.streetcomplete.ui.common.ZoomOutIcon
import de.westnordost.streetcomplete.util.logs.Log

/** Combined control for zooming in and out */
@Composable
fun ZoomButtons(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    zoom: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        elevation = 4.dp
    ) {
        Column(
            Modifier
                .width(IntrinsicSize.Min)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        zoom(-dragAmount / 30)
                    }
                }
        ) {
            IconButton(onClick = onZoomIn, enabled = enabled) { ZoomInIcon() }
            Divider()
            IconButton(onClick = onZoomOut, enabled = enabled) { ZoomOutIcon() }
        }
    }
}

@Preview
@Composable
private fun PreviewZoomButtons() {
    ZoomButtons(onZoomIn = {}, onZoomOut = {}, zoom = {})
}
