package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.logDebug
import de.westnordost.streetcomplete.ui.theme.logError
import de.westnordost.streetcomplete.ui.theme.logInfo
import de.westnordost.streetcomplete.ui.theme.logVerbose
import de.westnordost.streetcomplete.ui.theme.logWarning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogLevelFilterChips(
    selectedLogLevels: Set<LogLevel>,
    onSelectedLogLevels: (Set<LogLevel>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.label_log_level),
            style = MaterialTheme.typography.caption
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (logLevel in LogLevel.entries) {
                LogLevelFilterChip(
                    logLevel = logLevel,
                    selected = logLevel in selectedLogLevels,
                    onClick = {
                        onSelectedLogLevels(
                            if (logLevel in selectedLogLevels) selectedLogLevels - logLevel
                            else selectedLogLevels + logLevel
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogLevelFilterChip(
    logLevel: LogLevel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (selected) R.drawable.ic_check_circle_24dp else R.drawable.ic_circle_outline_24dp
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        colors = ChipDefaults.filterChipColors(
            contentColor = logLevel.color,
            selectedContentColor = MaterialTheme.colors.surface,
            selectedLeadingIconColor = MaterialTheme.colors.surface,
            selectedBackgroundColor = logLevel.color,
        ),
        leadingIcon = { Icon(painterResource(icon), null) },
    ) {
        Text(logLevel.name)
    }
}

val LogLevel.color: Color @Composable get() = when (this) {
    LogLevel.VERBOSE -> MaterialTheme.colors.logVerbose
    LogLevel.DEBUG -> MaterialTheme.colors.logDebug
    LogLevel.INFO -> MaterialTheme.colors.logInfo
    LogLevel.WARNING -> MaterialTheme.colors.logWarning
    LogLevel.ERROR -> MaterialTheme.colors.logError
}

@PreviewLightDark
@Composable
private fun LogLevelFilterChipsPreview() {
    AppTheme { Surface {
        var logLevels by remember { mutableStateOf(LogLevel.entries.toSet()) }
        LogLevelFilterChips(
            selectedLogLevels = logLevels,
            onSelectedLogLevels = { logLevels = it }
        )
    } }
}
