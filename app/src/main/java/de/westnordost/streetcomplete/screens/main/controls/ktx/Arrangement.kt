package de.westnordost.streetcomplete.screens.main.controls.ktx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Place items in reverse order, that is, from right to left / bottom to top, regardless of layout
 * direction.
 */
internal val Arrangement.Absolute.Reverse: Arrangement.HorizontalOrVertical
    get() = ReverseAbsoluteArrangement

@Stable
private data object ReverseAbsoluteArrangement : Arrangement.HorizontalOrVertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray,
    ) = placeReversed(sizes, outPositions)

    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) =
        placeReversed(sizes, outPositions)

    private fun placeReversed(sizes: IntArray, outPosition: IntArray) {
        var current = 0
        for (i in (sizes.size - 1) downTo 0) {
            outPosition[i] = current
            current += sizes[i]
        }
    }

    override fun toString() = "Arrangement.Absolute#Reverse"
}
