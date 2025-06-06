package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.asItem
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.theme.SelectionColor
import de.westnordost.streetcomplete.ui.theme.SelectionFrameColor
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.DisplayItem

@Composable
fun <T> SelectableIconRightItem(
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
    val imageBitmap = remember(item.image) {
        when (item.image) {
            is ResImage -> {
                val resDrawable = ContextCompat.getDrawable(context, (item.image as ResImage).resId)
                resDrawable?.toBitmap(resDrawable.intrinsicWidth, resDrawable.intrinsicHeight)?.asImageBitmap()
            }

            is DrawableImage -> (item.image as DrawableImage).drawable.toBitmap().asImageBitmap()
            else -> null
        }
    }
    val title = remember(item.title) {
        when (item.title) {
            is ResText -> context.getString((item.title as ResText).resId)
            is CharSequenceText -> (item.title as CharSequenceText).text.toString()
            null -> ""
        }
    }
    val description = remember(item.description) {
        when (item.description) {
            is ResText -> context.getString((item.description as ResText).resId)
            is CharSequenceText -> (item.description as CharSequenceText).text.toString()
            null -> null
        }
    }

    Box(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = role
            )
            .conditional(isSelected) {
                border(4.dp, SelectionFrameColor, RoundedCornerShape(16.dp))
            }
            .background(
                color = SelectionColor.copy(alpha = animatedAlpha),
                shape = RoundedCornerShape(16.dp)
            )
        ,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = title
                )
            }
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1
                )
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
}

@Composable
@Preview(showBackground = true)
fun SelectableIconRightItemPreview() {
    var selected by remember { mutableStateOf(false) }

    SelectableIconRightItem(
        item = BuildingType.APARTMENTS.asItem(),
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
