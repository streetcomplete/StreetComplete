package de.westnordost.streetcomplete.screens.user.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.avatar_osm_anonymous
import de.westnordost.streetcomplete.resources.ic_open_in_browser_24
import de.westnordost.streetcomplete.resources.ic_star_48
import de.westnordost.streetcomplete.resources.osm_profile
import de.westnordost.streetcomplete.resources.unsynced_quests_description
import de.westnordost.streetcomplete.resources.user_logout
import de.westnordost.streetcomplete.resources.user_profile_achievement_levels
import de.westnordost.streetcomplete.resources.user_profile_all_time_title
import de.westnordost.streetcomplete.resources.user_profile_current_week_title
import de.westnordost.streetcomplete.resources.user_profile_dates_mapped
import de.westnordost.streetcomplete.resources.user_profile_days_active
import de.westnordost.streetcomplete.resources.user_profile_global_rank
import de.westnordost.streetcomplete.resources.user_profile_local_rank
import de.westnordost.streetcomplete.ui.ktx.toDp
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import de.westnordost.streetcomplete.util.ktx.displayRegion
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Shows the user profile: username, avatar, star count and a hint regarding unpublished changes */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userAvatarFile by viewModel.userAvatarFile.collectAsState()

    val editCount by viewModel.editCount.collectAsState()
    val editCountCurrentWeek by viewModel.editCountCurrentWeek.collectAsState()
    val unsyncedChangesCount by viewModel.unsyncedChangesCount.collectAsState()

    val achievementLevels by viewModel.achievementLevels.collectAsState()
    val rank by viewModel.rank.collectAsState()
    val rankCurrentWeek by viewModel.rankCurrentWeek.collectAsState()
    val biggestSolvedCountCountryStatistics by viewModel.biggestSolvedCountCountryStatistics.collectAsState()
    val biggestSolvedCountCurrentWeekCountryStatistics by viewModel.biggestSolvedCountCurrentWeekCountryStatistics.collectAsState()

    val daysActive by viewModel.daysActive.collectAsState()
    val datesActive by viewModel.datesActive.collectAsState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Basic user info

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter =
                    userAvatarFile?.let { fileBitmapPainter(it.toString()) }
                    ?: painterResource(Res.drawable.avatar_osm_anonymous),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = userName.orEmpty(),
                    style = MaterialTheme.typography.headlineLarge
                )
                StarCount(editCount)
                if (unsyncedChangesCount > 0) {
                    Text(
                        text = stringResource(
                            Res.string.unsynced_quests_description,
                            unsyncedChangesCount
                        ),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        // User button row

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val uriHandler = LocalUriHandler.current
            Button(onClick = {
                uriHandler.openUri("https://www.openstreetmap.org/user/" + viewModel.userName.value)
            }) {
                Icon(painterResource(Res.drawable.ic_open_in_browser_24), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.osm_profile).uppercase())
            }
            OutlinedButton(onClick = { viewModel.logOutUser() }) {
                Text(stringResource(Res.string.user_logout).uppercase())
            }
        }

        Divider()

        // Statistics

        Text(
            text = stringResource(Res.string.user_profile_all_time_title),
            style = MaterialTheme.typography.titleLarge
        )

        var delay = 0

        if (editCount > 0) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val localStats = biggestSolvedCountCountryStatistics
                if (localStats?.rank != null) {
                    LocalRankBadge(localStats.rank, localStats.countryCode, getAnimationDelay(delay++))
                }
                if (rank > 0) {
                    RankBadge(rank, getAnimationDelay(delay++))
                }
                if (daysActive > 0) {
                    DaysActiveBadge(daysActive, getAnimationDelay(delay++))
                }
                if (achievementLevels > 0) {
                    AchievementLevelsBadge(achievementLevels, getAnimationDelay(delay++))
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.user_profile_current_week_title),
                style = MaterialTheme.typography.titleLarge
            )
            StarCount(editCountCurrentWeek)
        }
        if (editCountCurrentWeek > 0) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val localStats = biggestSolvedCountCurrentWeekCountryStatistics
                if (localStats?.rank != null) {
                    LocalRankCurrentWeekBadge(localStats.rank, localStats.countryCode, getAnimationDelay(delay++))
                }
                if (rankCurrentWeek > 0) {
                    RankCurrentWeekBadge(rankCurrentWeek, getAnimationDelay(delay++))
                }
            }
        }
        Text(
            text = stringResource(Res.string.user_profile_dates_mapped),
            style = MaterialTheme.typography.titleLarge
        )
        BoxWithConstraints {
            DatesActiveTable(
                datesActive = datesActive.datesActive.toSet(),
                datesActiveRange = datesActive.range,
                modifier = Modifier.width(maxWidth.coerceAtMost(640.dp))
            )
        }
    }
}

@Composable
private fun LocalRankBadge(rank: Int, countryCode: String, delay: Int) {
    LaurelWreathBadge(
        label = getLocalRankText(countryCode),
        value = "#$rank",
        progress = getLocalRankProgress(rank),
        animationDelay = delay
    )
}

@Composable
private fun RankBadge(rank: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(Res.string.user_profile_global_rank),
        value = "#$rank",
        progress = getRankProgress(rank),
        animationDelay = delay
    )
}

@Composable
private fun DaysActiveBadge(days: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(Res.string.user_profile_days_active),
        value = days.toString(),
        progress = ((days + 20) / 100f).coerceAtMost(1f),
        animationDelay = delay
    )
}

@Composable
private fun AchievementLevelsBadge(levels: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(Res.string.user_profile_achievement_levels),
        value = levels.toString(),
        progress = ((levels / 2) / 100f).coerceAtMost(1f),
        animationDelay = delay
    )
}

@Composable
private fun LocalRankCurrentWeekBadge(rank: Int, countryCode: String, delay: Int) {
    LaurelWreathBadge(
        label = getLocalRankText(countryCode),
        value = "#$rank",
        progress = getLocalRankCurrentWeekProgress(rank),
        animationDelay = delay
    )
}

@Composable
private fun RankCurrentWeekBadge(rank: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(Res.string.user_profile_global_rank),
        value = "#$rank",
        progress = getRankCurrentWeekProgress(rank),
        animationDelay = delay
    )
}

@Composable
private fun StarCount(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_star_48),
            contentDescription = null,
            modifier = Modifier.size(32.sp.toDp()) // icon should scale with the text
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun getLocalRankText(countryCode: String): String =
    stringResource(Res.string.user_profile_local_rank, getCountryName(countryCode))

private fun getCountryName(countryCode: String): String =
    // we don't use the language, but we need it for correct construction of the languageTag
    Locale("en-$countryCode").displayRegion ?: countryCode

private fun getAnimationDelay(step: Int) = step * 500
