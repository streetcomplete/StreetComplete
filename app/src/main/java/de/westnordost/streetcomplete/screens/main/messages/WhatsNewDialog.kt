package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.about.ChangelogList
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
        text = {
            ChangelogList(changelog = changelog, onClickLink = onClickLink)
        }
    )
}
