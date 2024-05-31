package de.westnordost.streetcomplete.screens.about

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.html.HtmlNode

/** A dialog that shows the changelog */
@Composable
fun WhatsNewDialog(
    changelog: Map<String, List<HtmlNode>>,
    onDismissRequest: () -> Unit,
    onClickLink: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.title_whats_new)) },
        text = { ChangelogList(changelog = changelog, onClickLink = onClickLink) }
    )
}
