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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

@Composable
fun BubblePile(
    count: Int,
    allIcons: List<DrawableResource>,
    modifier: Modifier = Modifier,
    bubbleSize: Dp = 50.dp
) {
    val bubbles = remember(count, allIcons) {
        (0..<count).map {
            BubblePlacement(
                icon = allIcons[Random.nextInt(0, allIcons.size)],
                x = Random.nextFloat(),
                y = Random.nextFloat()
            )
        }
    }
    BoxWithConstraints(modifier) {
        for (i in 0..<count) {
            val bubble = bubbles[i]
            Image(
                painter = painterResource(bubble.icon),
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

private data class BubblePlacement(val icon: DrawableResource, val x: Float, val y: Float)

@Preview
@Composable
private fun PreviewBubblePile() {
    BubblePile(
        count = 50,
        allIcons = listOf(
            Res.drawable.quest_bicycle_parking,
            Res.drawable.quest_building,
            Res.drawable.quest_drinking_water,
            Res.drawable.quest_notes,
            Res.drawable.quest_street_surface,
            Res.drawable.quest_wheelchair,
        ),
        modifier = Modifier.size(200.dp)
    )
}
