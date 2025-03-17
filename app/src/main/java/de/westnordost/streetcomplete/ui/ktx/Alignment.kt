package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

private val Alignment.horizontalBias: Float
    get() {
        val align = align(IntSize.Zero, IntSize(10_000_000, 1), LayoutDirection.Ltr)
        return align.x / 5_000_000f - 1f
    }

internal val Alignment.horizontal: Alignment.Horizontal
    get() = BiasAlignment.Horizontal(horizontalBias)

private val Alignment.verticalBias: Float
    get() {
        val align = align(IntSize.Zero, IntSize(1, 10_000_000), LayoutDirection.Ltr)
        return align.y / 5_000_000f - 1f
    }

internal val Alignment.vertical: Alignment.Vertical
    get() = BiasAlignment.Vertical(verticalBias)

internal fun Alignment.Horizontal.toArrangement(): Arrangement.Horizontal {
    val align = align(0, 2, LayoutDirection.Ltr)
    return when {
        align < 1 -> Arrangement.Start
        align == 1 -> Arrangement.Center
        align > 1 -> Arrangement.End
        else -> Arrangement.End
    }
}

internal fun Alignment.Vertical.toArrangement(): Arrangement.Vertical {
    val align = align(0, 2)
    return when {
        align < 1 -> Arrangement.Top
        align == 1 -> Arrangement.Center
        align > 1 -> Arrangement.Bottom
        else -> Arrangement.Top
    }
}

internal fun LayoutDirection.reverse(): LayoutDirection =
    when (this) {
        LayoutDirection.Ltr -> LayoutDirection.Rtl
        LayoutDirection.Rtl -> LayoutDirection.Ltr
    }
