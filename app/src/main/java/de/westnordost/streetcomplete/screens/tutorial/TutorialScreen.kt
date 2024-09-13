package de.westnordost.streetcomplete.screens.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Generic multiple-page tutorial screen */
@Composable
fun TutorialScreen(
    pageCount: Int,
    onDismissRequest: () -> Unit,
    onFinished: () -> Unit,
    onPageChanged: suspend (page: Int) -> Unit = {},
    dismissOnBackPress: Boolean = true,
    nextIsEnabled: (page: Int) -> Boolean = { true },
    illustration: @Composable BoxScope.(page: Int) -> Unit,
    pageContent: @Composable (page: Int) -> Unit,
) {
    val state = rememberPagerState { pageCount }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(state.currentPage > 0 || dismissOnBackPress) {
        if (state.currentPage > 0) {
            coroutineScope.launch {
                state.animateScrollToPage(state.currentPage - 1)
            }
        } else {
            onDismissRequest()
        }
    }
    LaunchedEffect(state.currentPage) {
        onPageChanged(state.currentPage)
    }

    Surface(Modifier.fillMaxSize()) {
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
                    userScrollEnabled = false,
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
                    nextIsEnabled = nextIsEnabled,
                    onLastPageFinished = {
                        onDismissRequest()
                        onFinished()
                    },
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
