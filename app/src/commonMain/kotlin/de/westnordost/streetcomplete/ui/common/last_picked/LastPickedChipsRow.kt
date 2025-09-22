package de.westnordost.streetcomplete.ui.common.last_picked

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.fadingEdges
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import kotlin.math.min

/** Row of chips for items previously picked */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <I> LastPickedChipsRow(
    items: List<I>,
    onClick: (I) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (I) -> Unit,
) {
    val state = rememberScrollState()
    val fadeWidth = 80
    val fadeStart = min(fadeWidth, state.value).pxToDp()
    val fadeEnd =
        if (state.maxValue != Int.MAX_VALUE) min(fadeWidth, state.maxValue - state.value).pxToDp()
        else 0.dp

    Box(modifier.fadingEdges(start = fadeStart, end = fadeEnd,)) {
        Row(
            modifier = Modifier.horizontalScroll(state),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (item in items) {
                Chip(onClick = { onClick(item) }) {
                    Box(Modifier.padding(vertical = 4.dp)) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}
