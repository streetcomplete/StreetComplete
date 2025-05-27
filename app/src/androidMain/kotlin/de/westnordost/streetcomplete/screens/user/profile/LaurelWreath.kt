package de.westnordost.streetcomplete.screens.user.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.LeafGreen

@Composable
fun LaurelWreath(
    modifier: Modifier = Modifier,
    color: Color = LeafGreen,
    progress: Float = 1f
) {
    if (progress < 0.1f) return

    val leafPair = painterResource(R.drawable.laurel_leaf_pair)
    val leafPairGrowing = painterResource(R.drawable.laurel_leaf_pair)
    val leafSingle = painterResource(R.drawable.laurel_leaf_ending)

    Box(modifier.aspectRatio(1f).drawBehind {
        val maxLeafs = 10f
        val leafs = (maxLeafs * progress).toInt()
        val leafSize = size * 2f / maxLeafs
        val leafY = center.y - leafSize.center.y
        val maxLeafAngle = 160.0f

        fun drawLeaf(leaf: Painter, angle: Float, scale: Float = 1f) {
            val offset = (1f - scale) * leafSize.width
            for (scaleX in listOf(1f, -1f)) {
                withTransform({
                    scale(scaleX, 1f)
                    rotate(angle)
                    translate(top = leafY + offset, left = offset / 2f)
                }) {
                    with(leaf) { draw(leafSize * scale, colorFilter = ColorFilter.tint(color)) }
                }
            }
        }

        // stalk
        inset(leafSize.width / 2.1f) {
            drawArc(
                color = color,
                startAngle = 90f - maxLeafAngle * progress,
                sweepAngle = maxLeafAngle * progress * 2,
                useCenter = false,
                style = Stroke(leafSize.width * 0.1f),
            )
        }

        // leaves left and right
        for (i in 0..<leafs) {
            drawLeaf(
                leaf = if (i == leafs - 1) leafPairGrowing else leafPair,
                angle = (i + 1f) * maxLeafAngle / maxLeafs - 90f,
                scale = if (i == leafs - 1) maxLeafs * progress % 1f else 1f
            )
        }

        // leading leaf
        drawLeaf(leafSingle, maxLeafAngle * progress - 90f)
    })
}

@Preview @Composable
private fun LaurelWreathBadgePreview() {
    LaurelWreath(progress = 1.0f)
}
