package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.asItem
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.toBitmap
import de.westnordost.streetcomplete.view.toString

@Composable
fun <T> SelectableIconRightCell(
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
    val description = remember(item.description) { item.description?.toString(context) }

    Row(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = role
            )
            .conditional(isSelected) {
                border(4.dp, MaterialTheme.colors.secondary, RoundedCornerShape(16.dp))
            }
            .background(
                color = MaterialTheme.colors.secondary.copy(alpha = animatedAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = title
            )
        }
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1
                )
            }
            if (description != null) {
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.body2,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SelectableIconRightCellPreview() {
    var selected by remember { mutableStateOf(false) }

    SelectableIconRightCell(
        item = BuildingType.APARTMENTS.asItem(),
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
