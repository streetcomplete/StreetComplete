package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.credits.Contributor
import de.westnordost.streetcomplete.data.credits.Credits
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.about_title_authors
import de.westnordost.streetcomplete.resources.credits_art_contributors_title
import de.westnordost.streetcomplete.resources.credits_author_title
import de.westnordost.streetcomplete.resources.credits_contributors
import de.westnordost.streetcomplete.resources.credits_contributors_title
import de.westnordost.streetcomplete.resources.credits_main_contributors_title
import de.westnordost.streetcomplete.resources.credits_projects_contributors_title
import de.westnordost.streetcomplete.resources.credits_translations_title
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.BulletSpan
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.ktx.displayName
import org.jetbrains.compose.resources.stringResource

/** Shows the credits of this app */
@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    onClickBack: () -> Unit
) {
    val credits by viewModel.credits.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.about_title_authors)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        credits?.let { credits ->
            SelectionContainer {
                CreditsSections(
                    credits = credits,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        ))
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
        CreditsSection(stringResource(Res.string.credits_author_title)) {
            HtmlText(
                html = credits.mainContributors.first().toTextWithLink(),
                style = TextStyle(fontWeight = FontWeight.Bold),
            )
        }
        CreditsSection(stringResource(Res.string.credits_main_contributors_title)) {
            for (contributor in credits.mainContributors.drop(1)) {
                BulletSpan { HtmlText(contributor.toTextWithLink()) }
            }
        }
        CreditsSection(stringResource(Res.string.credits_projects_contributors_title)) {
            for (contributor in credits.projectsContributors) {
                BulletSpan { HtmlText(contributor) }
            }
        }
        CreditsSection(stringResource(Res.string.credits_art_contributors_title)) {
            for (contributor in credits.artContributors) {
                BulletSpan { HtmlText(contributor) }
            }
        }
        CreditsSection(stringResource(Res.string.credits_contributors_title)) {
            for (contributor in credits.codeContributors) {
                BulletSpan { HtmlText(contributor.toTextWithLink()) }
            }
            BulletSpan { Text("…") }
            HtmlText(stringResource(Res.string.credits_contributors))
        }
        CreditsSection(stringResource(Res.string.credits_translations_title)) {
            // sorted list of (language name, list of translators sorted by contributions descending)
            val translatorsByDisplayLanguage = remember(credits.translatorsByLanguage) {
                credits.translatorsByLanguage
                    .map { (language, translators) ->
                        val languageName = Locale(language).displayName ?: language
                        val sortedTranslators = translators.entries
                            .sortedByDescending { it.value }
                            .map { it.key }
                        languageName to sortedTranslators
                    }
                    .sortedBy { it.first }
            }

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
        ProvideTextStyle(MaterialTheme.typography.body2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}
