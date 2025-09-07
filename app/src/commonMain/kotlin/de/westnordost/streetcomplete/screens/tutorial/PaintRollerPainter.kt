package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.util.svgPath

@Composable
fun paintRollerPainter(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 234.dp,
    defaultHeight = 234.dp,
    viewportWidth = 26f,
    viewportHeight = 26f,
    autoMirror = false
) { _, _ ->
    Path(
        pathData = rodPath,
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        stroke = SolidColor(Color(0xff788ca4))
    )
    Path(
        pathData = rodShinePath,
        strokeLineWidth = 0.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        stroke = SolidColor(Color(0xff9baabb))
    )
    Path(
        pathData = rollerPath,
        fill = SolidColor(Color(0xfffef0c1))
    )
    Group(clipPathData = rollerLintClipPath) {
        Group(translationY = progress.coerceIn(0f, 1f) * 9f) {
            Path(
                pathData = rollerLintPath,
                fill = SolidColor(Color(0xffe2cf90))
            )
        }
    }
    Path(
        pathData = rollerShadowPath,
        fill = SolidColor(Color(0xffe2cf90))
    )
    Path(
        pathData = handlePath,
        fill = SolidColor(Color(0xffef4431))
    )
    Path(
        pathData = gripPath,
        strokeLineWidth = 1f,
        stroke = SolidColor(Color(0xffac3024))
    )
}

private val rodPath = svgPath("m12.5,14v-3l12,-2v-5h-23")
private val rodShinePath = svgPath("m12.25,13.625v-3l12,-2v-5h-23")
private val rollerPath = svgPath("M3.5,1L21.5,1A1,1 0,0 1,22.5 2L22.5,6A1,1 0,0 1,21.5 7L3.5,7A1,1 0,0 1,2.5 6L2.5,2A1,1 0,0 1,3.5 1z")
private val rollerLintPath = svgPath("m11,-8v2h1v-2zM4,-7v2h1v-2zM15,-7v2h1v-2zM7,-6v2h1v-2zM19,-6v2h1v-2zM10,-4v2h1v-2zM3,-3v2h1v-2zM21,-3v2h1v-2zM16,-2v2h1v-2zM6,-1v2h1v-2zM11,1v2h1v-2zM4,2v2h1v-2zM15,2v2h1v-2zM7,3v2h1v-2zM19,3v2h1v-2z")
private val rollerLintClipPath = svgPath("M3.5,1L21.5,1A1,1 0,0 1,22.5 2L22.5,4A1,1 0,0 1,21.5 5L3.5,5A1,1 0,0 1,2.5 4L2.5,2A1,1 0,0 1,3.5 1z")
private val rollerShadowPath = svgPath("m4.5,5h16.104c0.496,0 0.896,-0.33 0.896,-0.979v-3.021s1,0 1,1v4.104c0,0.496 -0.4,0.896 -0.896,0.896h-18.104v-1s0,-1 1,-1z")
private val handlePath = svgPath("M11.5,13L13.5,13A1,1 0,0 1,14.5 14L14.5,24A1,1 0,0 1,13.5 25L11.5,25A1,1 0,0 1,10.5 24L10.5,14A1,1 0,0 1,11.5 13z M10.375,13L14.625,13A0.875,0.875 0,0 1,15.5 13.875L15.5,14.125A0.875,0.875 0,0 1,14.625 15L10.375,15A0.875,0.875 0,0 1,9.5 14.125L9.5,13.875A0.875,0.875 0,0 1,10.375 13z")
private val gripPath = svgPath("M13.5,16v8 M11.5,16v8")
