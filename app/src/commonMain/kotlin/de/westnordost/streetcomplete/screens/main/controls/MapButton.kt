package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_location_24
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/** Small floating button on top of the map */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
    contentPadding: Dp = 12.dp,
    content: @Composable (BoxScope.() -> Unit),
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Preview
@Composable
private fun PreviewMapButton() {
    MapButton(onClick = {}) {
        Icon(painterResource(Res.drawable.ic_location_24), null)
    }
}
