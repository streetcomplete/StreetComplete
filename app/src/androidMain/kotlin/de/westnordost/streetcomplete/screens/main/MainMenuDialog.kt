package de.westnordost.streetcomplete.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.controls.NotificationBox
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeColorCircle
import de.westnordost.streetcomplete.ui.common.DownloadIcon
import de.westnordost.streetcomplete.ui.common.TeamModeIcon
import de.westnordost.streetcomplete.ui.common.UploadIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainMenuDialog(
    onDismissRequest: () -> Unit,
    onClickProfile: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickDownload: () -> Unit,
    onClickUpload: () -> Unit,
    onClickEnterTeamMode: () -> Unit,
    onClickExitTeamMode: () -> Unit,
    isLoggedIn: Boolean,
    indexInTeam: Int?,
    unsyncedEditsCount: Int?,
    isUploadingOrDownloading: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor
        ) {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BigMenuButton(
                        onClick = { onDismissRequest(); onClickProfile() },
                        icon = { Icon(painterResource(R.drawable.ic_profile_48dp), null) },
                        text = stringResource(
                            if (isLoggedIn) R.string.user_profile else R.string.user_login
                        ),
                    )
                    BigMenuButton(
                        onClick = { onDismissRequest(); onClickSettings() },
                        icon = { Icon(painterResource(R.drawable.ic_settings_48dp), null) },
                        text = stringResource(R.string.action_settings),
                    )
                    BigMenuButton(
                        onClick = { onDismissRequest(); onClickAbout() },
                        icon = { Icon(painterResource(R.drawable.ic_info_outline_48dp), null) },
                        text = stringResource(R.string.action_about2),
                    )
                }
                Divider()
                CompactMenuButton(
                    onClick = { onDismissRequest(); onClickDownload() },
                    icon = { DownloadIcon() },
                    text = stringResource(R.string.action_download),
                )
                if (unsyncedEditsCount != null) {
                    CompactMenuButton(
                        onClick = { onDismissRequest(); onClickUpload() },
                        icon = {
                            UploadIcon()
                            if (unsyncedEditsCount > 0) {
                                NotificationBox {
                                    Text(unsyncedEditsCount.toString(), textAlign = TextAlign.Center)
                                }
                            }
                        },
                        text = stringResource(R.string.action_upload),
                        enabled = !isUploadingOrDownloading,
                    )
                }
                if (indexInTeam == null) {
                    CompactMenuButton(
                        onClick = { onDismissRequest(); onClickEnterTeamMode() },
                        icon = { TeamModeIcon() },
                        text = stringResource(R.string.team_mode)
                    )
                } else {
                    CompactMenuButton(
                        onClick = { onDismissRequest(); onClickExitTeamMode() },
                        icon = {
                            TeamModeColorCircle(
                                index = indexInTeam,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = stringResource(R.string.team_mode_exit)
                    )
                }
            }
        }
    }
}

@Composable
private fun BigMenuButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CompactMenuButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMainMenuDialog() {
    MainMenuDialog(
        onDismissRequest = {},
        onClickProfile = {},
        onClickSettings = {},
        onClickAbout = {},
        onClickDownload = {},
        onClickUpload = {},
        onClickEnterTeamMode = {},
        onClickExitTeamMode = {},
        isLoggedIn = true,
        indexInTeam = 0,
        unsyncedEditsCount = 122,
        isUploadingOrDownloading = true,
    )
}
