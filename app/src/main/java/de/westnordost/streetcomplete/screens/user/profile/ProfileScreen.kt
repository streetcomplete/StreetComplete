package de.westnordost.streetcomplete.screens.user.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.util.toDp
import de.westnordost.streetcomplete.util.ktx.openUri
import java.util.Locale

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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Basic user info

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = getAvatarPainter(userAvatarFile.path)
                    ?: painterResource(R.drawable.ic_osm_anon_avatar),
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
                            R.string.unsynced_quests_description,
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
            val context = LocalContext.current
            Button(onClick = {
                context.openUri("https://www.openstreetmap.org/user/" + viewModel.userName.value)
            }) {
                Icon(painterResource(R.drawable.ic_open_in_browser_24dp), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.osm_profile).uppercase())
            }
            OutlinedButton(onClick = { viewModel.logOutUser() }) {
                Text(stringResource(R.string.user_logout).uppercase())
            }
        }

        Divider()

        // Statistics

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
                text = stringResource(R.string.user_profile_current_week_title),
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
            text = stringResource(R.string.user_profile_dates_mapped),
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
private fun LocalRankBadge(rank: Int, countryCode: String?, delay: Int) {
    LaurelWreathBadge(
        label = getLocalRankText(countryCode),
        value = "#${rank}",
        // 2024-05: rank 850 is about top 50% of users (~20 edits), rank 200 top 5% (~1500 edits)
        //          in Italy, which is the top 5 country in terms of contributions
        progress = getRankProgress(rank, maxProgressAtRank = 200, minProgressAtRank = 850),
        animationDelay = delay
    )
}

@Composable
private fun RankBadge(rank: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(R.string.user_profile_global_rank),
        value = "#$rank",
        // 2024-05: rank 5000 is about top 50% of users (~200 edits), rank 1500 top 5% (~5000 edits)
        progress = getRankProgress(rank, maxProgressAtRank = 1500, minProgressAtRank = 5000),
        animationDelay = delay
    )
}

@Composable
private fun DaysActiveBadge(days: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(R.string.user_profile_days_active),
        value = days.toString(),
        progress = ((days + 20) / 100f).coerceAtMost(1f),
        animationDelay = delay
    )
}

@Composable
private fun AchievementLevelsBadge(levels: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(R.string.user_profile_achievement_levels),
        value = levels.toString(),
        progress = ((levels / 2) / 100f).coerceAtMost(1f),
        animationDelay = delay
    )
}

@Composable
private fun LocalRankCurrentWeekBadge(rank: Int, countryCode: String?, delay: Int) {
    LaurelWreathBadge(
        label = getLocalRankText(countryCode),
        value = "#$rank",
        // 2024-05: rank 50 is about top 50% of users (~20 edits), rank 10 top 10% (~250 edits)
        //          in Italy, which is the top 5 country in terms of contributions
        progress = getRankProgress(rank, maxProgressAtRank = 10, minProgressAtRank = 50),
        animationDelay = delay
    )
}

@Composable
private fun RankCurrentWeekBadge(rank: Int, delay: Int) {
    LaurelWreathBadge(
        label = stringResource(R.string.user_profile_global_rank),
        value = "#$rank",
        // 2024-05: rank 370 is about top 50% of users (~20 edits), rank 100 top 5% (~300 edits)
        progress = getRankProgress(rank, maxProgressAtRank = 100, minProgressAtRank = 370),
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
            painter = painterResource(R.drawable.ic_star_48dp),
            contentDescription = null,
            modifier = Modifier.size(32.sp.toDp()) // icon should scale with the text
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

private fun getAvatarPainter(filename: String?): Painter? =
    filename?.let { BitmapFactory.decodeFile(it) }?.asImageBitmap()?.let { BitmapPainter(it) }

private fun getAnimationDelay(step: Int) = step * 500

@Composable
private fun getLocalRankText(countryCode: String?): String =
    stringResource(R.string.user_profile_local_rank, Locale("", countryCode ?: "").displayCountry)

/** Translate the user's actual rank to a value from 0 (bad) to 1 (the best) */
private fun getRankProgress(rank: Int, maxProgressAtRank: Int, minProgressAtRank: Int): Float =
    ((minProgressAtRank - rank).toFloat() / (minProgressAtRank - maxProgressAtRank))
        .coerceIn(0f, 1f)
