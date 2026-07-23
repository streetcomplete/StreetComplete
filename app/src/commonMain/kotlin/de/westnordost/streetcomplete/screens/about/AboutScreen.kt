package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.util.ktx.displayLanguage
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.ui.ktx.tryOpenUri
import org.koin.compose.koinInject

@Composable
fun AboutScreen(
    onClickChangelog: () -> Unit,
    onClickCredits: () -> Unit,
    onClickPrivacyStatement: () -> Unit,
    onClickLogs: () -> Unit,
    onClickBack: () -> Unit,
    appStoreInfo: AppStoreInfo = koinInject()
) {
    var showDonateDialog by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.action_about2)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ))
        ) {

            PreferenceCategory(
                stringResource(Res.string.about_title, ApplicationConstants.NAME),
            ) {

                Preference(
                    name = stringResource(Res.string.about_title_changelog),
                    onClick = { onClickChangelog() },
                ) {
                    Text("v" + BuildConfig.VERSION_NAME)
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(Res.string.about_title_authors),
                    onClick = { onClickCredits() },
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_license),
                    onClick = { uriHandler.tryOpenUri("https://www.gnu.org/licenses/gpl-3.0.html") },
                ) {
                    Text("GPLv3")
                    OpenInBrowserIcon()
                }

                Preference(
                    name = stringResource(Res.string.about_title_privacy_statement),
                    onClick = { onClickPrivacyStatement() },
                ) { NextScreenIcon() }
            }

            PreferenceCategory(stringResource(Res.string.about_category_help)) {
                Preference(
                    name = stringResource(Res.string.about_title_intro),
                    onClick = { showIntroTutorial = true },
                )

                Preference(
                    name = stringResource(Res.string.about_title_faq),
                    onClick = { uriHandler.tryOpenUri("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_report_error),
                    onClick = { uriHandler.tryOpenUri("https://github.com/streetcomplete/StreetComplete/issues") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_show_logs),
                    onClick = { onClickLogs() },
                    description = stringResource(Res.string.about_summary_logs),
                ) { NextScreenIcon() }
            }

            PreferenceCategory(stringResource(Res.string.about_category_contribute)) {

                Preference(
                    name = stringResource(Res.string.about_title_donate),
                    onClick = { showDonateDialog = true },
                    description = stringResource(Res.string.about_summary_donate),
                )

                Preference(
                    name = stringResource(Res.string.about_title_translate),
                    onClick = { uriHandler.tryOpenUri("https://poeditor.com/join/project/IE4GC127Ki") },
                    description = stringResource(
                        Res.string.about_description_translate,
                        Locale.current.displayLanguage ?: Locale.current.language,
                        stringResource(Res.string.translation_completeness)
                    )
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_repository),
                    onClick = { uriHandler.tryOpenUri("https://github.com/streetcomplete/StreetComplete") },
                ) { OpenInBrowserIcon() }
            }

            PreferenceCategory(stringResource(Res.string.about_category_feedback)) {

                val ratingUri = appStoreInfo.getRatingUri()
                if (ratingUri != null) {
                    Preference(
                        name = stringResource(Res.string.about_title_rate),
                        onClick = { uriHandler.tryOpenUri(ratingUri) },
                    ) { OpenInBrowserIcon() }
                }

                Preference(
                    name = stringResource(Res.string.about_title_feedback),
                    onClick = { uriHandler.tryOpenUri("https://github.com/streetcomplete/StreetComplete/discussions") },
                ) { OpenInBrowserIcon() }
            }
        }
    }

    if (showDonateDialog) {
        if (appStoreInfo.disallowsInAppDonationLinks()) {
            AltDonationsDialog { showDonateDialog = false }
        } else {
            DonationsDialog(
                onDismissRequest = { showDonateDialog = false },
                onClickLink = { uriHandler.tryOpenUri(it) }
            )
        }
    }

    AnimatedScreenVisibility(showIntroTutorial) {
        IntroTutorialScreen(
            onDismissRequest = { showIntroTutorial = false },
            dismissOnBackPress = true
        )
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen({}, {}, {}, {}, {})
}
