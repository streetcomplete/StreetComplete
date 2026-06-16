package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.InfoFilledIcon
import de.westnordost.streetcomplete.ui.common.InfoOutlineIcon
import de.westnordost.streetcomplete.ui.ktx.fadingHorizontalScrollEdges
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Layout that contains the title, subtitle (name and location label), info button and
 *  retractable info area */
@Composable
fun QuestHeader(
    title: String,
    subtitle: AnnotatedString?,
    hintText: String?,
    hintImages: List<DrawableResource>,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) {
                    Text(title)
                }
                if (subtitle != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.body1,
                        LocalContentAlpha provides ContentAlpha.medium
                    ) {
                        Text(subtitle)
                    }
                }
            }
            if (hintText != null || hintImages.isNotEmpty()) {
                IconToggleButton(
                    checked = showInfo,
                    onCheckedChange = { showInfo = it },
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                        if (showInfo) InfoFilledIcon() else InfoOutlineIcon()
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showInfo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hintText != null) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.body2) {
                        Text(hintText)
                    }
                }
                if (!hintImages.isNullOrEmpty()) {
                    HintImages(hintImages)
                }
            }
        }
    }
}


@Composable
private fun HintImages(
    hintImages: List<DrawableResource>,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    LazyRow(
        state = state,
        modifier = modifier.fadingHorizontalScrollEdges(state.scrollIndicatorState, 64.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hintImages) { hintImage ->
            Image(
                painter = painterResource(hintImage),
                contentDescription = null,
                modifier = Modifier.sizeIn(maxHeight = 180.dp)
            )
        }
    }
}
