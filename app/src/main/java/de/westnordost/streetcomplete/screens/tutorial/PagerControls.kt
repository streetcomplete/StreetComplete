package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.conditional
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerControls(
    state: PagerState,
    nextIsEnabled: (page: Int) -> Boolean,
    onLastPageFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    fun onClickNext() {
        if (state.isOnLastPage()) {
            onLastPageFinished()
        } else {
            coroutineScope.launch { state.animateScrollToPage(state.currentPage + 1) }
        }
    }

    fun onClickPager(page: Int) {
        coroutineScope.launch { state.animateScrollToPage(page) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Row {
            var enabled = true
            repeat(state.pageCount) { page ->
                PagerIndicator(
                    isCurrentPage = state.currentPage == page,
                    enabled = enabled,
                    onClick = { onClickPager(page) }
                )
                enabled = enabled && nextIsEnabled(page)
            }
        }
        Button(
            onClick = ::onClickNext,
            enabled = nextIsEnabled(state.currentPage)
        ) {
            Text(stringResource(if (state.isOnLastPage()) R.string.letsgo else R.string.next))
        }
    }
}

@Composable
private fun PagerIndicator(
    isCurrentPage: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isCurrentPage) 1f else 0.5f)
    val alpha by animateFloatAsState(when {
        isCurrentPage -> ContentAlpha.high
        enabled -> ContentAlpha.medium
        else -> ContentAlpha.disabled
    })
    Box(
        modifier
            .conditional(enabled) { clickable { onClick() } }
            .alpha(alpha)
            .padding(4.dp)
            .scale(scale)
            .size(14.dp)
            .background(color = MaterialTheme.colors.onSurface, shape = CircleShape)
    )
}

@OptIn(ExperimentalFoundationApi::class)
private fun PagerState.isOnLastPage(): Boolean = currentPage >= pageCount - 1
