package de.westnordost.streetcomplete.util.ktx

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset

fun PointF.toOffset() = Offset(x, y)
