package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Surface that looks like a max weight sign */
@Composable
fun MaxWeightSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)) {
        ProhibitorySign(
            modifier = modifier.size(256.dp),
            color = color
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

/** Surface that looks like a max weight sign in MUTCD countries */
@Composable
fun MaxWeightSignMutcd(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RectangularSign(modifier.width(160.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(4.dp),
        ) {
            ProvideTextStyle(MaterialTheme.typography.largeInput.copy(fontWeight = FontWeight.Bold)) {
                Text(text)
            }
            ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)) {
                content()
            }
        }
    }
}

/** Surface that looks like a max weight sign with an extra sign below (de: Zusatztafel) */
@Composable
fun MaxWeightSignExtra(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.White,
    signContent: @Composable BoxScope.() -> Unit,
    extraContent: @Composable BoxScope.() -> Unit,
) {
    val textStyle = MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)
    ProvideTextStyle(textStyle) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProhibitorySign(
                modifier = Modifier.size(256.dp),
                color = color,
                content = signContent,
            )
            RectangularSign(color = color) {
                Box(Modifier.padding(4.dp)) { extraContent() }
            }
        }
    }
}
