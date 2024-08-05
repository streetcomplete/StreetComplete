package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** Notification text shown on the top end corner of e.g. a button */
@Composable
fun BoxScope.MapButtonNotificationText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .background(MaterialTheme.colors.secondary, RoundedCornerShape(12.dp))
            .padding(vertical = 2.dp, horizontal = 6.dp),
        color = Color.White,
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun PreviewMapButtonWithNotificationText() {
    Box {
        MapButton(onClick = {}) {
            Icon(painterResource(R.drawable.ic_email_24dp), null)
        }
        MapButtonNotificationText(text = "999")
    }
}
