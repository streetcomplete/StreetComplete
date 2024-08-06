package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** Small floating button on top of the map */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    content: @Composable (BoxScope.() -> Unit),
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(6.dp),
        enabled = enabled,
        shape = CircleShape,
        color = color,
        contentColor = contentColor,
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Preview
@Composable
private fun PreviewMapButton() {
    MapButton(onClick = {}) {
        Icon(painterResource(R.drawable.ic_location_24dp), null)
    }
}
