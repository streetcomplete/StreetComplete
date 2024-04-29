package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.achievements
import de.westnordost.streetcomplete.screens.user.links.LazyLinksColumn
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleMedium
import de.westnordost.streetcomplete.ui.util.backgroundWithPadding
import de.westnordost.streetcomplete.util.ktx.openUri

@Composable
fun AchievementDetailsDialog(
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
        val interactionSource = remember { MutableInteractionSource() }

        if (isNew) {
            AnimatedTadaShine()
        }
        // center everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource, null) {
                    // dismiss when clicking wherever - no ripple effect
                    onDismissRequest()
                },
            contentAlignment = Alignment.Center
        ) {
            // in landscape layout, dialog would become too tall to fit
            BoxWithConstraints(Modifier.padding(16.dp)) {
                val isLandscape = maxWidth > maxHeight

                // scale down icon to fit small devices
                val iconSize = (min(maxWidth, maxHeight) * 0.67f).coerceAtMost(320.dp)

                val backgroundPadding =
                    if (isLandscape) PaddingValues(start = iconSize * 0.75f)
                    else PaddingValues(top = iconSize  * 0.75f)


                val dialogModifier = modifier
                    .backgroundWithPadding(
                        color = MaterialTheme.colors.surface,
                        padding = backgroundPadding,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(24.dp)

                val contentColor = contentColorFor(MaterialTheme.colors.surface)
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    if (isLandscape) {
                        Row(
                            modifier = dialogModifier.width(maxWidth.coerceAtMost(720.dp)),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AchievementIcon(achievement.icon, level, Modifier.size(iconSize))
                            AchievementDetails(
                                achievement, level,
                                horizontalAlignment = Alignment.Start,
                                isNew = isNew
                            )
                        }
                    } else {
                        Column(
                            modifier = dialogModifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AchievementIcon(achievement.icon, level, Modifier.size(iconSize))
                            AchievementDetails(
                                achievement, level,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                isNew = isNew
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementDetails(
    achievement: Achievement,
    level: Int,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    isNew: Boolean,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = stringResource(achievement.title),
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
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
        if (unlockedLinks.isNotEmpty() && isNew) {
            val unlockedLinksText = stringResource(
                if (unlockedLinks.size == 1) R.string.achievements_unlocked_link
                else R.string.achievements_unlocked_links
            )
            Text(
                text = unlockedLinksText,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium
            )
            val context = LocalContext.current
            LazyLinksColumn(
                links = unlockedLinks,
                onClickLink = { context.openUri(it) }
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@PreviewLightDark
@Composable
fun PreviewAchievementDetailsDialog() {
    AppTheme {
        AchievementDetailsDialog(
            achievement = achievements.associateBy { it.id }["regular"]!!,
            level = 7,
            onDismissRequest = {}
        )
    }
}
