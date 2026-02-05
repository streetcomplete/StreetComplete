package de.westnordost.streetcomplete.screens.about

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.about_category_contribute
import de.westnordost.streetcomplete.resources.about_category_feedback
import de.westnordost.streetcomplete.resources.about_category_help
import de.westnordost.streetcomplete.resources.about_description_translate
import de.westnordost.streetcomplete.resources.about_summary_donate
import de.westnordost.streetcomplete.resources.about_summary_logs
import de.westnordost.streetcomplete.resources.about_title_authors
import de.westnordost.streetcomplete.resources.about_title_changelog
import de.westnordost.streetcomplete.resources.about_title_donate
import de.westnordost.streetcomplete.resources.about_title_faq
import de.westnordost.streetcomplete.resources.about_title_feedback
import de.westnordost.streetcomplete.resources.about_title_intro
import de.westnordost.streetcomplete.resources.about_title_license
import de.westnordost.streetcomplete.resources.about_title_privacy_statement
import de.westnordost.streetcomplete.resources.about_title_rate
import de.westnordost.streetcomplete.resources.about_title_report_error
import de.westnordost.streetcomplete.resources.about_title_repository
import de.westnordost.streetcomplete.resources.about_title_show_logs
import de.westnordost.streetcomplete.resources.about_title_translate
import de.westnordost.streetcomplete.resources.action_about2
import de.westnordost.streetcomplete.resources.translation_completeness
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

@Composable
fun AboutScreen(
    onClickChangelog: () -> Unit,
    onClickCredits: () -> Unit,
    onClickPrivacyStatement: () -> Unit,
    onClickLogs: () -> Unit,
    onClickBack: () -> Unit,
) {
    var showDonateDialog by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }

    val context = LocalContext.current
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

            PreferenceCategory(null) {

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
                    onClick = { uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
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
                    onClick = { uriHandler.openUri("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_report_error),
                    onClick = { uriHandler.openUri("https://github.com/streetcomplete/StreetComplete/issues") },
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
                    onClick = { uriHandler.openUri("https://poeditor.com/join/project/IE4GC127Ki") },
                    description = stringResource(
                        Res.string.about_description_translate,
                        Locale.current.displayLanguage ?: Locale.current.language,
                        stringResource(Res.string.translation_completeness)
                    )
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(Res.string.about_title_repository),
                    onClick = { uriHandler.openUri("https://github.com/streetcomplete/StreetComplete") },
                ) { OpenInBrowserIcon() }
            }

            PreferenceCategory(stringResource(Res.string.about_category_feedback)) {

                if (context.isInstalledViaGooglePlay()) {
                    Preference(
                        name = stringResource(Res.string.about_title_rate),
                        onClick = { uriHandler.openUri("market://details?id=${context.packageName}") },
                    ) { OpenInBrowserIcon() }
                }

                Preference(
                    name = stringResource(Res.string.about_title_feedback),
                    onClick = { uriHandler.openUri("https://github.com/streetcomplete/StreetComplete/discussions") },
                ) { OpenInBrowserIcon() }
            }
        }
    }

    if (showDonateDialog) {
        if (BuildConfig.IS_FROM_MONOPOLISTIC_APP_STORE) {
            DonationsGooglePlayDialog { showDonateDialog = false }
        } else {
            DonationsDialog(
                onDismissRequest = { showDonateDialog = false },
                onClickLink = { uriHandler.openUri(it) }
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

private fun Context.isInstalledViaGooglePlay(): Boolean =
    applicationContext.packageManager.getInstallerPackageName(applicationContext.packageName) == "com.android.vending"

@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen({}, {}, {}, {}, {})
}
