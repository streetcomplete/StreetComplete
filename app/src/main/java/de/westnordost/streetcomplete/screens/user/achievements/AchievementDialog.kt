package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.achievements
import de.westnordost.streetcomplete.screens.user.DialogContentWithIconLayout
import de.westnordost.streetcomplete.screens.user.links.LazyLinksColumn
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleMedium

@Composable
fun AchievementDialog(
    achievement: Achievement,
    level: Int,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isNew: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (isNew) {
            AnimatedTadaShine()
        }
        // center everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                // dismiss when clicking wherever - no ripple effect
                .clickable(null, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            DialogContentWithIconLayout(
                icon = {
                    AchievementIcon(achievement.icon, level)
                },
                content = { isLandscape ->
                    AchievementDetails(
                        achievement, level,
                        isLandscape = isLandscape,
                        showLinks = isNew
                    )
                },
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun AchievementDetails(
    achievement: Achievement,
    level: Int,
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    showLinks: Boolean,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(achievement.title),
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
        )
        val description = achievement.description
        if (description != null) {
            val arg = achievement.getPointThreshold(level)
            Text(
                text = stringResource(description, arg),
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.body2
            )
        }
        val unlockedLinks = achievement.unlockedLinks[level].orEmpty()
        if (unlockedLinks.isNotEmpty() && showLinks) {
            val unlockedLinksText = stringResource(
                if (unlockedLinks.size == 1) R.string.achievements_unlocked_link
                else R.string.achievements_unlocked_links
            )
            Text(
                text = unlockedLinksText,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium
            )
            LazyLinksColumn(
                links = unlockedLinks
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewAchievementDetailsDialog() {
    AppTheme {
        AchievementDialog(
            achievement = achievements.associateBy { it.id }["regular"]!!,
            level = 7,
            onDismissRequest = {}
        )
    }
}
