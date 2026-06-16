package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.util.rememberPath

/** Animatable scissors. [progress] = 1 is closed, = 0 is open*/
@Composable
fun scissorsPainter(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 96.dp,
    defaultHeight = 96.dp,
    viewportWidth = 44f,
    viewportHeight = 44f,
    autoMirror = false,
) { _, _ ->
    val p = progress.coerceIn(0f, 1f)
    Group(
        rotation = progress * 29f,
        pivotX = 22f,
        pivotY = 22f,
    ) {
        Path(
            pathData = rememberPath("m29,34c-2.3906,-4 -1.9361,-3.2881 -3,-5 0.1925,-1.5267 1,-4 1,-4 0.9758,1.3862 8,-0 11,3 3,3 2,7 -0,9 -2,2 -6.6094,1 -9,-3zM36,30c-2,-2 -5.88,-1.1144 -6,-1 -0.6234,0.5944 0.3766,1.6953 2,4 1.6234,2.3047 3,3 4,2 1,-1 2,-3 -0,-5z"),
            fill = SolidColor(Color(0xffdd2e44))
        )
        Path(
            pathData = rememberPath("m12,4c-1.163,0.657 -1.658,2.836 -1,4l9,16c1,2 5,3 6,5 1.103,-0.496 0.394,-2.401 1,-4z"),
            fill = SolidColor(Color(0xff99aab5))
        )
    }
    Group(
        rotation = p * -29f,
        pivotX = 22f,
        pivotY = 22f,
    ) {
        Path(
            pathData = rememberPath("m15,34c2.3906,-4 1.9361,-3.2881 3,-5 -0.1925,-1.5267 -1,-4 -1,-4 -0.9758,1.3862 -8,-0 -11,3 -3,3 -2,7 -0,9 2,2 6.6094,1 9,-3zM8,30c2,-2 5.88,-1.1144 6,-1 0.6234,0.5944 -0.3766,1.6953 -2,4 -1.6234,2.3047 -3,3 -4,2 -1,-1 -2,-3 -0,-5z"),
            fill = SolidColor(Color(0xffdd2e44))
        )
        Path(
            pathData = rememberPath("m32,4c1.163,0.657 1.658,2.836 1,4l-9,16c-1,2 -5,3 -6,5 -1.103,-0.496 -0.394,-2.401 -1,-4z"),
            fill = SolidColor(Color(0xffccd6dd))
        )
    }

    Path(
        pathData = rememberPath("M22,22m-1.3722,0a1.3722,1.3722 0,1 1,2.7445 0a1.3722,1.3722 0,1 1,-2.7445 0"),
        fill = SolidColor(Color(0xff99aab5))
    )
}
