package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.titleLarge

@Composable
fun AchievementIcon(
    icon: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.achievement_frame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(1f)
                .shadow(elevation = 4.dp, shape = AchievementFrameShape)
        )
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.91f)
        )
        if (level > 1) {
            Text(
                text = level.toString(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .levelLabelBackground(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun Modifier.levelLabelBackground() =
    drawBehind {
        val radius = CornerRadius(32.dp.toPx(), 32.dp.toPx())
        drawRoundRect(
            color = Color(0xff9b51e0),
            cornerRadius = radius
        )
        drawRoundRect(
            color = Color(0xfffbbb00),
            cornerRadius = radius,
            style = Stroke(4.dp.toPx())
        )
    }
    .padding(horizontal = 10.dp, vertical = 4.dp)

object AchievementFrameShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val pathStr = "m0.55404 0.97761c-0.029848 0.029846-0.078236 0.029846-0.10808 0l-0.42357-0.42357c-0.029848-0.029848-0.029848-0.078239 0-0.10808l0.42357-0.42357c0.029846-0.029846 0.078236-0.029846 0.10808 0l0.42357 0.42357c0.029846 0.029846 0.029846 0.078236 0 0.10808z"
        val path = PathParser().parsePathString(pathStr).toPath()
        path.transform(Matrix().apply { scale(size.width, size.height, 1f) })
        return Outline.Generic(path)
    }
}

@Preview
@Composable
fun PreviewAchievementIcon() {
    AchievementIcon(icon = R.drawable.ic_achievement_first_edit, level = 8)
}
