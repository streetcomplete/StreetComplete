package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.util.ktx.openUri

/** Shows the privacy statement */
@Composable
fun PrivacyStatementScreen(
    vectorTileProvider: VectorTileProvider,
    onClickBack: () -> Unit
) {
    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title_privacy_statement)) },
            navigationIcon = { IconButton(onClick = { onClickBack() }) { BackIcon() } },
        )
        SelectionContainer {
            val context = LocalContext.current
            HtmlText(
                html =
                    stringResource(R.string.privacy_html) +
                    stringResource(R.string.privacy_html_tileserver2, vectorTileProvider.title, vectorTileProvider.privacyStatementLink) +
                    stringResource(R.string.privacy_html_statistics) +
                    stringResource(R.string.privacy_html_third_party_quest_sources) +
                    stringResource(R.string.privacy_html_image_upload2),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                onClickLink = { context.openUri(it) }
            )
        }
    }
}
