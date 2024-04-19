package de.westnordost.streetcomplete.ui.user.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.DisabledGray
import de.westnordost.streetcomplete.ui.theme.GrassGreen
import de.westnordost.streetcomplete.ui.theme.LeafGreen

@Composable
fun LaurelWreath(
    modifier: Modifier = Modifier,
    startBackgroundColor: Color = DisabledGray,
    finalBackgroundColor: Color = GrassGreen,
    progress: Float = 1f,
    content: @Composable BoxScope.() -> Unit
) {
    val leafPair = painterResource(R.drawable.laurel_leaf_pair)
    val leafSingle = painterResource(R.drawable.laurel_leaf_ending)
    val progress = progress.coerceIn(0f, 1f)

    Box(
        modifier
            .aspectRatio(1f)
            .drawBehind {
                drawCircle(color = startBackgroundColor)
                drawCircle(color = finalBackgroundColor, alpha = progress)

                val maxLeafs = 10f
                val leafs = (maxLeafs * progress).toInt()
                val leafSize = size * 2f / maxLeafs
                val leafY = center.y - leafSize.center.y
                val maxLeafAngle = 165.0f

                inset(size.width * 0.025f) {
                    for (i in 0..<leafs) {
                        val painter = if (i + 1 < leafs) leafPair else leafSingle
                        val rotation = (i + 1f) * maxLeafAngle / maxLeafs - 90f
                        // left side
                        withTransform({
                            rotate(rotation)
                            translate(top = leafY)
                        }) {
                            with(painter) { draw(leafSize) }
                        }
                        // right side
                        withTransform({
                            scale(-1f, 1f)
                            rotate(rotation)
                            translate(top = leafY)
                        }) {
                            with(painter) { draw(leafSize) }
                        }
                    }

                    inset(leafSize.width / 2.2f) {
                        drawArc(
                            color = LeafGreen,
                            startAngle = 90f - maxLeafAngle * progress,
                            sweepAngle = maxLeafAngle * progress * 2,
                            useCenter = false,
                            style = Stroke(size.width * 0.02f)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Preview @Composable
fun LaurelWreathPreview() {
    LaurelWreath {}
}
