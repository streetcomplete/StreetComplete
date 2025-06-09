package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental
import de.westnordost.streetcomplete.quests.boat_rental.asItem
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.toBitmap
import de.westnordost.streetcomplete.view.toString

@Composable
fun <T> SelectableIconCell(
    item: DisplayItem<T>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.Checkbox
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 0f,
        label = "OverlayAlpha"
    )
    val context = LocalContext.current
    val imageBitmap = remember(item.image) { item.image?.toBitmap(context)?.asImageBitmap() }
    val title = remember(item.title) { item.title?.toString(context) }

    Box(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = role
            )
            .conditional(isSelected) {
                border(4.dp, MaterialTheme.colors.secondary, RoundedCornerShape(8.dp))
            }
            .padding(4.dp)
            .background(
                color = MaterialTheme.colors.secondary.copy(alpha = animatedAlpha),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = title,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    contentScale = ContentScale.Inside,
                    alignment = Alignment.Center
                )
            }
            if (title != null) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SelectableIconCellPreview() {
    var selected by remember { mutableStateOf(false) }

    SelectableIconCell(
        item = BoatRental.CANOE.asItem(),
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
