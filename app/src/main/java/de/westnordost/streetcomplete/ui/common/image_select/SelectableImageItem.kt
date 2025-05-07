package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R

@Composable
fun SelectableImageItem(
    imageResId: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 0f,
        label = "OverlayAlpha"
    )

    Box(
        modifier = Modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Checkbox
            )
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null
        )

        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Yellow.copy(alpha = animatedAlpha))
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SelectableImagePreview() {
    var selected by remember { mutableStateOf(false) }

    SelectableImageItem(
        imageResId = R.drawable.bicycle_parking_type_shed, // replace with an actual drawable
        title = "My Image",
        isSelected = selected,
        onClick = { selected = !selected }
    )
}
