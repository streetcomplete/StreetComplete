package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun <T> SelectableIconItem(
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .conditional(isSelected) {
                border(4.dp, SelectionFrameColor, RoundedCornerShape(5.dp))
                    .background(SelectionColor.copy(alpha = animatedAlpha))
            }
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()

        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = title,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(start = 6.dp, end = 6.dp, top = 6.dp)
                            .wrapContentSize(Alignment.Center),
                        contentScale = ContentScale.Inside,
                        alignment = Alignment.Center
                    )
                }

                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        fontSize = 12.sp,
                        shadow = Shadow(
                            color = Color.Gray,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    ),
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
fun SelectableIconPreview() {
    var selected by remember { mutableStateOf(false) }

    SelectableIconItem(
        item = BoatRental.CANOE.asItem(),
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
