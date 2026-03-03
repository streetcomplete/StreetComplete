package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.achievements
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.achievements_unlocked_link
import de.westnordost.streetcomplete.resources.achievements_unlocked_links
import de.westnordost.streetcomplete.screens.user.DialogContentWithIconLayout
import de.westnordost.streetcomplete.screens.user.links.LazyLinksColumn
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleMedium
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AchievementDialog(
    achievement: Achievement,
    level: Int,
    unlockedLinks: List<Link>,
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
                    achievement.icon?.let { AchievementIcon(painterResource(it), level) }
                },
                content = { isLandscape ->
                    AchievementDetails(
                        achievement, level, unlockedLinks,
                        isLandscape = isLandscape
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
    unlockedLinks: List<Link>,
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
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
        if (unlockedLinks.isNotEmpty()) {
            val unlockedLinksText = stringResource(
                if (unlockedLinks.size == 1) Res.string.achievements_unlocked_link
                else Res.string.achievements_unlocked_links
            )
            Text(
                text = unlockedLinksText,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium
            )
            LazyLinksColumn(
                links = unlockedLinks,
                // force a maximum size for the lazy column so that it may live inside a scrollable
                // container
                modifier = Modifier.heightIn(max = 360.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewAchievementDetailsDialog() {
    AppTheme {
        val regularAchievement = achievements.associateBy { it.id }["regular"]!!
        AchievementDialog(
            achievement = regularAchievement,
            level = 7,
            unlockedLinks = regularAchievement.unlockedLinks[7].orEmpty(),
            onDismissRequest = {}
        )
    }
}
