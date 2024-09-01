package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.screens.user.DialogContentWithIconLayout
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.util.ktx.openUri

/** Shows the details for a certain quest type in a custom dialog. */
@Composable
fun EditTypeDialog(
    editType: EditType,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) { // center everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                // dismiss when clicking wherever - no ripple effect
                .clickable(remember { MutableInteractionSource() }, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            DialogContentWithIconLayout(
                icon = {
                    Image(
                        painter = painterResource(editType.icon),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().shadow(elevation = 4.dp, CircleShape)
                    )
                },
                content = { isLandscape ->
                    EditTypeDetails(
                        editType = editType,
                        isLandscape = isLandscape
                    )
                },
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun EditTypeDetails(
    editType: EditType,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(editType.title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
        )

        editType.wikiLink?.let { wikiLink ->
            OutlinedButton(
                onClick = { context.openUri("https://wiki.openstreetmap.org/wiki/$wikiLink") }
            ) {
                OpenInBrowserIcon()
                Text(
                    text = stringResource(R.string.user_statistics_quest_wiki_link),
                    modifier = Modifier.padding(start = 8.dp),
                    textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
                )
            }
        }
    }
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewEditTypeDialog() {
    EditTypeDialog(
        editType = AddRecyclingType(),
        onDismissRequest = {}
    )
}
