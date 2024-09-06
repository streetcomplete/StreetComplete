package de.westnordost.streetcomplete.screens.user

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsScreen
import de.westnordost.streetcomplete.screens.user.edits.EditStatisticsScreen
import de.westnordost.streetcomplete.screens.user.links.LinksScreen
import de.westnordost.streetcomplete.screens.user.profile.ProfileScreen
import de.westnordost.streetcomplete.ui.common.BackIcon
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Shows the tabs with the user profile, user statistics, achievements and links */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserScreen(
    onClickBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(pageCount = { UserTab.entries.size })
        UserScreenTopAppBar(
            onClickBack = onClickBack,
            pagerState = pagerState
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.Top,
        ) { p ->
            when (UserTab.entries[p]) {
                UserTab.Profile -> {
                    ProfileScreen(viewModel = koinViewModel())
                }
                UserTab.Statistics -> {
                    EditStatisticsScreen(viewModel = koinViewModel())
                }
                UserTab.Achievements -> {
                    AchievementsScreen(viewModel = koinViewModel())
                }
                UserTab.Links -> {
                    LinksScreen(viewModel = koinViewModel())
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserScreenTopAppBar(
    onClickBack: () -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.primarySurface,
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        Column {
            TopAppBar(
                title = { Text(stringResource(R.string.user_profile)) },
                navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
                elevation = 0.dp
            )

            val scope = rememberCoroutineScope()
            val page = pagerState.targetPage

            BoxWithConstraints {
                TabRow(selectedTabIndex = page) {
                    for (tab in UserTab.entries) {
                        val icon = painterResource(tab.iconId)
                        val text = stringResource(tab.textId)
                        val index = tab.ordinal
                        val showText = min(maxWidth, maxHeight) >= 600.dp
                        Tab(
                            selected = page == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            icon = { Icon(icon, text) },
                            text = if (showText) {
                                { Text(text) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class UserTab(
    val iconId: Int,
    val textId: Int,
) {
    Profile(
        iconId = R.drawable.ic_profile_24dp,
        textId = R.string.user_profile_title,
    ),
    Statistics(
        iconId = R.drawable.ic_star_24dp,
        textId = R.string.user_quests_title,
    ),
    Achievements(
        iconId = R.drawable.ic_achievements_24dp,
        textId = R.string.user_achievements_title
    ),
    Links(
        iconId = R.drawable.ic_bookmarks_24dp,
        textId = R.string.user_links_title
    ),
}
