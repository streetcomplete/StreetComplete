package de.westnordost.streetcomplete.ui.common.last_picked

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.SuggestionChip
import de.westnordost.streetcomplete.ui.ktx.fadingHorizontalScrollEdges

/** Row of chips for items previously picked */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <I> LastPickedChipsRow(
    items: List<I>,
    onClick: (I) -> Unit,
    modifier: Modifier = Modifier,
    chipBorder: BorderStroke? = null,
    chipContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    itemContent: @Composable (I) -> Unit,
) {
    val state = rememberScrollState()
    Row(
        modifier = modifier
            .fadingHorizontalScrollEdges(state, 32.dp)
            .horizontalScroll(state),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (item in items) {
            SuggestionChip(
                onClick = { onClick(item) },
                border = chipBorder,
                contentPadding = chipContentPadding,
            ) {
                Box(Modifier.padding(vertical = 4.dp)) {
                    itemContent(item)
                }
            }
        }
    }
}
