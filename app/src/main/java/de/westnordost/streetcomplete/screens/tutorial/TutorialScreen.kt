package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    illustration: @Composable (pageAnimated: Float) -> Unit,
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    val state = rememberPagerState { pageCount }
    val coroutineScope = rememberCoroutineScope()
    val pageAnimated = remember(state.currentPage, state.currentPageOffsetFraction) {
        derivedStateOf { state.currentPage + state.currentPageOffsetFraction }
    }
// TODO deal with landscape layout?

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.weight(0.5f),
            contentAlignment = Alignment.BottomCenter
        ) {
            illustration(pageAnimated.value)
        }
        HorizontalPager(
            state = state,
            modifier = Modifier.weight(0.5f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 32.dp,
            pageContent = pageContent
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                repeat(pageCount) { page ->
                    PagerIndicator(isCurrentPage = state.currentPage == page)
                }
            }
            Button(onClick = {
                if (state.isOnLastPage()) {
                    onFinished()
                } else {
                    coroutineScope.launch {
                        state.animateScrollToPage(state.currentPage + 1)
                    }
                }
            }) {
                Text(stringResource(if (state.isOnLastPage()) R.string.letsgo else R.string.next))
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        if (isCurrentPage) ContentAlpha.high else ContentAlpha.disabled
    )
    Box(modifier
        .padding(4.dp)
        .alpha(alpha)
        .background(color = MaterialTheme.colors.onSurface, shape = CircleShape)
        .size(12.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
private fun PagerState.isOnLastPage(): Boolean = currentPage >= pageCount - 1
