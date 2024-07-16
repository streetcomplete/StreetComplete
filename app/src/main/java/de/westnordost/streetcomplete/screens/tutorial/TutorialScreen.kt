package de.westnordost.streetcomplete.screens.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import kotlinx.coroutines.launch

/** Generic multiple-page tutorial screen */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    pageCount: Int,
    onFinished: () -> Unit,
    illustration: @Composable BoxScope.(page: Int) -> Unit,
    pageContent: @Composable (page: Int) -> Unit
) {
    val state = rememberPagerState { pageCount }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(state.currentPage > 0) {
        coroutineScope.launch {
            state.animateScrollToPage(state.currentPage - 1)
        }
    }

    TutorialScreenLayout(
        illustration = {
            illustration(state.currentPage)
        },
        pageContent = {
            HorizontalPager(
                state = state,
                modifier = Modifier.width(480.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 64.dp,
                pageContent = { page ->
                    Box(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 96.dp)
                    ) {
                        pageContent(page)
                    }
                }
            )
        },
        controls = {
            PagerControls(
                state = state,
                onLastPageFinished = onFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            .0f to Color.Transparent,
                            .5f to MaterialTheme.colors.surface
                        )
                    )
                    .padding(bottom = 16.dp)
            )
        },
        modifier = Modifier.safeDrawingPadding()
    )
}

@Composable
private fun TutorialScreenLayout(
    modifier: Modifier = Modifier,
    illustration: @Composable BoxScope.() -> Unit,
    pageContent: @Composable () -> Unit,
    controls: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        if (maxHeight > maxWidth) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.4f)
                        .clipToBounds(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    illustration()
                }
                Box(
                    modifier = Modifier.weight(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    pageContent()
                    Box(Modifier.align(Alignment.BottomCenter)) {
                        controls()
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.4f)
                            .clipToBounds(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        illustration()
                    }
                    Box(
                        modifier = Modifier.weight(0.6f),
                        contentAlignment = Alignment.Center
                    ) {
                        pageContent()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.weight(0.4f))
                    Box(
                        Modifier
                            .weight(0.6f)
                            .align(Alignment.Bottom)) {
                        controls()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerControls(
    state: PagerState,
    onLastPageFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Row {
            repeat(state.pageCount) { page ->
                PagerIndicator(
                    isCurrentPage = state.currentPage == page,
                    onClick = {
                        coroutineScope.launch { state.animateScrollToPage(page) }
                    }
                )
            }
        }
        Button(onClick = {
            if (state.isOnLastPage()) {
                onLastPageFinished()
            } else {
                coroutineScope.launch { state.animateScrollToPage(state.currentPage + 1) }
            }
        }) {
            Text(stringResource(if (state.isOnLastPage()) R.string.letsgo else R.string.next))
        }
    }
}

@Composable
private fun PagerIndicator(
    isCurrentPage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        if (isCurrentPage) ContentAlpha.high else ContentAlpha.disabled
    )
    Box(
        modifier
            .padding(4.dp)
            .alpha(alpha)
            .background(color = MaterialTheme.colors.onSurface, shape = CircleShape)
            .size(12.dp)
            .clickable { onClick() }
    )
}

@OptIn(ExperimentalFoundationApi::class)
private fun PagerState.isOnLastPage(): Boolean = currentPage >= pageCount - 1
