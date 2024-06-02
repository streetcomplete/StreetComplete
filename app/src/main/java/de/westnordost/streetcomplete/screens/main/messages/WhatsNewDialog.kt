package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.about.ChangelogList
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.util.html.HtmlNode

/** A dialog that shows the changelog */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WhatsNewDialog(
    changelog: Map<String, List<HtmlNode>>,
    onDismissRequest: () -> Unit,
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // AlertDialog does not support scrollable content (yet) https://issuetracker.google.com/issues/217151230
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            contentColor = contentColorFor(MaterialTheme.colors.surface)
        ) {
            Column() {
                Text(
                    text = stringResource(R.string.title_whats_new),
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp, end = 24.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
                ChangelogList(
                    changelog = changelog,
                    onClickLink = onClickLink,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .weight(1f),
                    paddingValues = PaddingValues(vertical = 16.dp)
                )
                Divider()
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(android.R.string.ok))
                    }
                }

            }
        }
    }
}
