package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.ktx.openUri

/** Shows the full changelog */
@Composable
fun ChangelogScreen(
    viewModel: ChangelogViewModel,
    onClickBack: () -> Unit
) {
    val changelog by viewModel.changelog.collectAsState()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title_changelog)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        changelog?.let { changelog ->
            SelectionContainer {
                ChangelogList(
                    changelog = changelog,
                    onClickLink = { context.openUri(it) },
                    paddingValues = PaddingValues(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChangelogList(
    changelog: Map<String, List<HtmlNode>>,
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = paddingValues
    ) {
        itemsIndexed(
            items = changelog.entries.toList(),
            key = { index, _ -> index }
        ) { index, (version, html) ->
            if (index > 0) Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text(text = version, style = MaterialTheme.typography.titleLarge)
            HtmlText(
                html = html,
                style = MaterialTheme.typography.body2,
                onClickLink = onClickLink
            )
        }
    }
}
