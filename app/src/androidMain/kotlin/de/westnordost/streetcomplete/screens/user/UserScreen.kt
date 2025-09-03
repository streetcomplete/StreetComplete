package de.westnordost.streetcomplete.screens.user

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_achievements_24
import de.westnordost.streetcomplete.resources.ic_bookmarks_24
import de.westnordost.streetcomplete.resources.ic_profile_24
import de.westnordost.streetcomplete.resources.ic_star_24
import de.westnordost.streetcomplete.resources.user_achievements_title
import de.westnordost.streetcomplete.resources.user_links_title
import de.westnordost.streetcomplete.resources.user_profile
import de.westnordost.streetcomplete.resources.user_profile_title
import de.westnordost.streetcomplete.resources.user_quests_title
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsScreen
import de.westnordost.streetcomplete.screens.user.edits.EditStatisticsScreen
import de.westnordost.streetcomplete.screens.user.links.LinksScreen
import de.westnordost.streetcomplete.screens.user.profile.ProfileScreen
import de.westnordost.streetcomplete.ui.common.BackIcon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Shows the tabs with the user profile, user statistics, achievements and links */
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
                title = { Text(stringResource(Res.string.user_profile)) },
                windowInsets = AppBarDefaults.topAppBarWindowInsets,
                navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
                elevation = 0.dp
            )

            val scope = rememberCoroutineScope()
            val page = pagerState.targetPage

            BoxWithConstraints {
                TabRow(
                    selectedTabIndex = page,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
                ) {
                    for (tab in UserTab.entries) {
                        val icon = painterResource(tab.icon)
                        val text = stringResource(tab.text)
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
    val icon: DrawableResource,
    val text: StringResource,
) {
    Profile(
        icon = Res.drawable.ic_profile_24,
        text = Res.string.user_profile_title,
    ),
    Statistics(
        icon = Res.drawable.ic_star_24,
        text = Res.string.user_quests_title,
    ),
    Achievements(
        icon = Res.drawable.ic_achievements_24,
        text = Res.string.user_achievements_title
    ),
    Links(
        icon = Res.drawable.ic_bookmarks_24,
        text = Res.string.user_links_title
    ),
}
