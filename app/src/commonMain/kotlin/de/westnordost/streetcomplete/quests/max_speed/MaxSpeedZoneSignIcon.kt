package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.dpToSp
import de.westnordost.streetcomplete.ui.ktx.pxToSp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor

/** An icon for a generic max speed (slow) zone sign */
@Composable
fun MaxSpeedZoneSignIcon(
    zoneLabel: String,
    zoneIsAtTop: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(128.dp)
            .background(backgroundColor)
            .padding(4.dp)
            .border(2.dp, TrafficSignColor.Black, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 24.dp.dpToSp())
        if (zoneIsAtTop) BasicText(zoneLabel, style = textStyle)

        Box(Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(TrafficSignColor.Red, CircleShape)
            .padding(12.dp)
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .background(TrafficSignColor.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
        )

        if (!zoneIsAtTop) BasicText(zoneLabel, style = textStyle)
    }
}
