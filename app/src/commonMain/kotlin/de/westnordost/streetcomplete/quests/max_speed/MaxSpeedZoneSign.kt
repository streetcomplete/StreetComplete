package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.ktx.dpToSp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.trafficSignContentColorFor

/** Surface that looks like generic a max speed zone sign (rectangle with a prohibitory sign in the
 *  middle and a label above or below) */
@Composable
fun MaxSpeedZoneSign(
    zoneLabel: String?,
    zoneIsAtTop: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TrafficSignColor.White,
    color: Color = TrafficSignColor.White,
    content: @Composable BoxScope.() -> Unit,
) {
    RectangularSign(
        modifier = modifier.size(256.dp),
        color = backgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            val textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            if (zoneLabel != null && zoneIsAtTop) Text(zoneLabel, style = textStyle)

            ProhibitorySign(
                modifier = Modifier.weight(1f),
                color = color,
                content = content
            )

            if (zoneLabel != null && !zoneIsAtTop) Text(zoneLabel, style = textStyle)
        }
    }
}
