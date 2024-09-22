package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** Notification shown on the top end corner of e.g. a button */
@Composable
fun BoxScope.MapButtonNotification(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White,
        LocalTextStyle provides MaterialTheme.typography.caption
    ) {
        Box(
            modifier = modifier
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colors.secondaryVariant, RoundedCornerShape(12.dp))
                .padding(vertical = 2.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Preview
@Composable
private fun PreviewMapButtonWithNotification() {
    Box {
        MapButton(onClick = {}) {
            Icon(painterResource(R.drawable.ic_email_24dp), null)
        }
        MapButtonNotification {
            Text(text = "999")
        }
    }
}
