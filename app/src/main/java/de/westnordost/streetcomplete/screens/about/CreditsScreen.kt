package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.BulletSpan
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.ui.theme.titleLarge
import java.util.Locale

/** Shows the credits of this app */
@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    onClickBack: () -> Unit
) {
    val credits by viewModel.credits.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title_authors)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        credits?.let { credits ->
            SelectionContainer {
                CreditsSections(
                    credits = credits,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CreditsSections(
    credits: Credits,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        CreditsSection(stringResource(R.string.credits_author_title)) {
            HtmlText(
                html = credits.mainContributors.first().toTextWithLink(),
                style = TextStyle(fontWeight = FontWeight.Bold),
            )
        }
        CreditsSection(stringResource(R.string.credits_main_contributors_title)) {
            for (contributor in credits.mainContributors.drop(1)) {
                BulletSpan { HtmlText(contributor.toTextWithLink()) }
            }
        }
        CreditsSection(stringResource(R.string.credits_projects_contributors_title)) {
            for (contributor in credits.projectsContributors) {
                BulletSpan { HtmlText(contributor) }
            }
        }
        CreditsSection(stringResource(R.string.credits_art_contributors_title)) {
            for (contributor in credits.artContributors) {
                BulletSpan { HtmlText(contributor) }
            }
        }
        CreditsSection(stringResource(R.string.credits_contributors_title)) {
            for (contributor in credits.codeContributors) {
                BulletSpan { HtmlText(contributor.toTextWithLink()) }
            }
            BulletSpan { Text("…") }
            HtmlText(stringResource(R.string.credits_contributors))
        }
        CreditsSection(stringResource(R.string.credits_translations_title)) {
            val translatorsByDisplayLanguage = credits.translators
                .map { Locale.forLanguageTag(it.key).displayName to it.value }
                .sortedBy { it.first }

            for ((language, translators) in translatorsByDisplayLanguage) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = language,
                        modifier = Modifier.width(120.dp),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(translators.joinToString())
                }
            }
        }
    }
}

private fun Contributor.toTextWithLink(): String = when (githubUsername) {
    null -> name
    name -> "<a href=\"$githubLink\">$githubUsername</a>"
    else -> "$name (<a href=\"$githubLink\">$githubUsername</a>)"
}

@Composable
private fun CreditsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit),
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.body2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}
