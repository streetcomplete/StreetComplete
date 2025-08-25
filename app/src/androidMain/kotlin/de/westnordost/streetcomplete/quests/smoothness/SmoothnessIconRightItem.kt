package de.westnordost.streetcomplete.quests.smoothness

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.segregated.CyclewaySegregation
import de.westnordost.streetcomplete.quests.segregated.asItem
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.DisplayItem

@Composable
fun <T> SmoothnessIconRightItem(
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
    val smoothness = item.value as de.westnordost.streetcomplete.quests.smoothness.Smoothness?
    val title = remember(item.title) {
        buildAnnotatedString {
            append((item.title as CharSequenceText).text)
            append(" ")
            appendInlineContent(id = smoothness?.name ?: "blank")
        }
    }
    val inlineContentMap = mapOf(
        (smoothness?.name ?: "blank") to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            val iconPainter = painterResource(smoothness?.icon ?: R.drawable.ic_smoothness_skateboard)
            Image(
                painter = iconPainter,
                modifier = Modifier.fillMaxSize(),
                contentDescription = ""
            )
        }
    )

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
                border(4.dp, MaterialTheme.colors.secondary)
            }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colors.secondary.copy(alpha = animatedAlpha))
        )
        Row (verticalAlignment = Alignment.CenterVertically) {
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = title.text,
                    modifier = Modifier
                        .wrapContentSize(Alignment.CenterStart)
                )
            }
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(start = 8.dp)) {
                Text(title,
                    style = TextStyle(
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        fontSize = 13.sp
                    ),
                    inlineContent = inlineContentMap
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = TextStyle(
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SmoothnessIconPreview() {
    var selected by remember { mutableStateOf(false) }

    SmoothnessIconRightItem(
        item = CyclewaySegregation.SIDEWALK.asItem(isLeftHandTraffic = true),
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
