package de.westnordost.streetcomplete.ui.common.bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubble
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleNoArrow
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import de.westnordost.streetcomplete.ui.ktx.isLandscape
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.Dimensions

/** A [BottomSheet] form that features a [header] at the top, below, a [note], then below, a
 * vertically nested scrollable [content]. Optionally, there can be a [fab] (floating action button)
 * in the bottom end corner. */
@Composable
fun BottomSheetFormScaffold(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    note: @Composable (() -> Unit)? = null,
    elevation: Dp = 4.dp,
    initialState: BottomSheetState =
        if (LocalWindowInfo.current.isLandscape) BottomSheetState.Expanded
        else BottomSheetState.Collapsed,
    peekHeight: Dp = Dimensions.getQuestFormPeekHeight(LocalWindowInfo.current)
) {
    val windowInfo = LocalWindowInfo.current
    var bottomSheetHeightPx by remember { mutableIntStateOf(0) }
    val bottomSheetPeekHeight = peekHeight.toPx()

    // Keep the sheet's drag bounds above the keyboard, including when collapsed.
    Box(modifier = modifier
        .imePadding()
        .sizeIn(maxWidth = Dimensions.getMaxQuestFormWidth(windowInfo))
    ) {
        BottomSheet(
            initialState = initialState,
            peekHeight = peekHeight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { bottomSheetHeightPx = it.height }
                    .safeDrawingPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (header != null) {
                    SpeechBubble(
                        elevation = elevation,
                        arrowDirection = SpeechBubbleArrowDirection.Top,
                        arrowPlacementBias = 0.1f,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        contentPadding =
                            PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
                        content = {
                            Column(Modifier.fillMaxWidth()) {
                                if (bottomSheetHeightPx > bottomSheetPeekHeight) {
                                    BottomSheetDragHandle(Modifier.padding(bottom = 8.dp))
                                } else {
                                    Spacer(Modifier.size(4.dp))
                                }
                                Box { header() }
                            }
                        }
                    )
                }

                if (note != null) {
                    SpeechBubbleNoArrow(
                        elevation = elevation,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        note()
                    }
                }

                SpeechBubbleNoArrow(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = elevation,
                    contentPadding = PaddingValues.Zero
                ) {
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fadingVerticalScrollEdges(scrollState, 32.dp)
                            .verticalScroll(scrollState),
                        content = { content() }
                    )
                }
            }
        }
        if (fab != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeDrawingPadding()
                    .padding(8.dp),
                content = { fab() }
            )
        }
    }
}

@Composable
private fun ColumnScope.BottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Box(modifier
        .size(40.dp, 4.dp)
        .background(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.16f),
            shape = RoundedCornerShape(4.dp)
        )
        .align(Alignment.CenterHorizontally)
    )
}
