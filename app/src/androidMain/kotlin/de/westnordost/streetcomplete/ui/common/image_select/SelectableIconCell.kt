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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental
import de.westnordost.streetcomplete.quests.boat_rental.asItem
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.theme.SelectionColor
import de.westnordost.streetcomplete.ui.theme.SelectionFrameColor
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.DisplayItem

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

    Box(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = role
            )
            .conditional(isSelected) {
                border(4.dp, SelectionFrameColor, RoundedCornerShape(8.dp))
            }
            .padding(4.dp)
            .background(
                color = SelectionColor.copy(alpha = animatedAlpha),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = title,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    contentScale = ContentScale.Inside,
                    alignment = Alignment.Center
                )
            }

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
