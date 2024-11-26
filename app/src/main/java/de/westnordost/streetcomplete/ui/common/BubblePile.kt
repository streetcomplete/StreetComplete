package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import kotlin.random.Random

@Composable
fun BubblePile(
    count: Int,
    allIconsIds: List<Int>,
    modifier: Modifier = Modifier,
    bubbleSize: Dp = 50.dp
) {
    val bubbles = remember(count, allIconsIds) {
        (0..<count).map {
            BubblePlacement(
                iconId = allIconsIds[Random.nextInt(0, allIconsIds.size)],
                x = Random.nextFloat(),
                y = Random.nextFloat()
            )
        }
    }
    BoxWithConstraints(modifier) {
        for (i in 0..<count) {
            val bubble = bubbles[i]
            Image(
                painter = painterResource(bubble.iconId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(
                        x = (maxWidth - bubbleSize) * bubble.x,
                        y = (maxHeight - bubbleSize) * bubble.y,
                    )
                    .requiredSize(bubbleSize)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

private data class BubblePlacement(val iconId: Int, val x: Float, val y: Float)

@Preview
@Composable
private fun PreviewBubblePile() {
    BubblePile(
        count = 50,
        allIconsIds = listOf(
            R.drawable.ic_quest_bicycle_parking,
            R.drawable.ic_quest_building,
            R.drawable.ic_quest_drinking_water,
            R.drawable.ic_quest_notes,
            R.drawable.ic_quest_street_surface,
            R.drawable.ic_quest_wheelchair,
        ),
        modifier = Modifier.size(200.dp)
    )
}
