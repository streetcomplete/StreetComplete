package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import kotlin.math.roundToInt

/** Bar chart row that shows a title, the count and the bar in the selected color */
@Composable
fun BarChartRow(
    title: @Composable BoxScope.() -> Unit,
    count: Float,
    maxCount: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box { title() }
        BoxWithConstraints(Modifier.weight(1f)) {
            val textMeasurer = rememberTextMeasurer(1)
            val textStyle = MaterialTheme.typography.body1
            val textSize = textMeasurer.measure(count.roundToInt().toString(), textStyle).size
            val availableBarWidth = maxWidth - textSize.width.pxToDp() - 8.dp
            val barWidth = (availableBarWidth * count / maxCount)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(32.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Spacer(Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(2.dp, 8.dp, 8.dp, 2.dp))
                    )
                }

                Text(
                    text = count.roundToInt().toString(),
                    style = textStyle,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStatisticsRow() {
    BarChartRow(
        title = {
            Image(
                painter = painterResource(R.drawable.ic_building_allotment_house),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        count = 68f,
        maxCount = 100
    )
}
