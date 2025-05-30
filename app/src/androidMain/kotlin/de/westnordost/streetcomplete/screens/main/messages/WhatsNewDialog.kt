package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.screens.about.ChangelogList
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog

/** A dialog that shows the changelog */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WhatsNewDialog(
    changelog: Changelog,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(R.string.title_whats_new)) },
        content = {
            ChangelogList(
                changelog = changelog,
                paddingValues = PaddingValues(vertical = 16.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}
