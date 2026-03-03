package de.westnordost.streetcomplete.overlays

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.quest_select_hint
import de.westnordost.streetcomplete.ui.ktx.minus
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A card that displays one item, styled like a selector */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <I> ItemCard(
    item: I? = null,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (item: I) -> Unit
) {
    Card(
        onClick = { onExpandChange(!expanded) },
        modifier = modifier,
        border = ButtonDefaults.outlinedBorder,
        elevation = 0.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(ButtonDefaults.ContentPadding - PaddingValues(end = 8.dp))
        ) {
            AnimatedContent(
                targetState = item,
                modifier = Modifier.weight(1f, fill = false),
                contentAlignment = Alignment.Center,
                transitionSpec = FallDownTransitionSpec,
            ) { item ->
                if (item == null) {
                    Text(stringResource(Res.string.quest_select_hint))
                } else {
                    content(item)
                }
            }
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }
    }
}
