package de.westnordost.streetcomplete.screens.about

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import java.util.Locale

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
            title = { Text(stringResource(R.string.action_about2) + " SCEE") },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            PreferenceCategory(null) {

                Preference(
                    name = stringResource(R.string.about_title_changelog),
                    onClick = { onClickChangelog() },
                ) {
                    Text("v" + BuildConfig.VERSION_NAME)
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(R.string.about_title_authors),
                    onClick = { onClickCredits() },
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(R.string.about_title_license),
                    onClick = { uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
                ) {
                    Text("GPLv3")
                    OpenInBrowserIcon()
                }

                Preference(
                    name = stringResource(R.string.about_title_privacy_statement),
                    onClick = { onClickPrivacyStatement() },
                ) { NextScreenIcon() }
            }

            PreferenceCategory(stringResource(R.string.about_category_help)) {
                Preference(
                    name = stringResource(R.string.about_title_intro),
                    onClick = { showIntroTutorial = true },
                )

                Preference(
                    name = stringResource(R.string.about_title_faq),
                    onClick = { uriHandler.openUri("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_report_error),
                    onClick = { uriHandler.openUri("https://github.com/helium314/SCEE/issues") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_show_logs),
                    onClick = { onClickLogs() },
                    description = stringResource(R.string.about_summary_logs),
                ) { NextScreenIcon() }
            }

            PreferenceCategory(stringResource(R.string.about_category_contribute)) {

                Preference(
                    name = stringResource(R.string.about_title_donate),
                    onClick = { showDonateDialog = true },
                    description = stringResource(R.string.about_summary_donate),
                )

                Preference(
                    name = stringResource(R.string.about_title_translate),
                    onClick = { uriHandler.openUri("https://poeditor.com/join/project/IE4GC127Ki") },
                    description = stringResource(
                        R.string.about_description_translate,
                        Locale.getDefault().displayLanguage,
                        integerResource(R.integer.translation_completeness)
                    )
                ) { OpenInBrowserIcon() }

                Preference(
                    name = "SCEE: " + stringResource(R.string.about_title_translate),
                    onClick = { uriHandler.openUri("https://translate.codeberg.org/projects/scee/") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_repository),
                    onClick = { uriHandler.openUri("https://github.com/streetcomplete/StreetComplete") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = "SCEE " + stringResource(R.string.about_title_repository),
                    onClick = { uriHandler.openUri("https://github.com/streetcomplete/StreetComplete") },
                ) { OpenInBrowserIcon() }
            }

            PreferenceCategory(stringResource(R.string.about_category_feedback)) {

                if (context.isInstalledViaGooglePlay()) {
                    Preference(
                        name = stringResource(R.string.about_title_rate),
                        onClick = { uriHandler.openUri("market://details?id=${context.packageName}") },
                    ) { OpenInBrowserIcon() }
                }

                Preference(
                    name = stringResource(R.string.about_title_feedback),
                    onClick = { uriHandler.openUri("https://github.com/Helium314/SCEE/discussions/") },
                ) { OpenInBrowserIcon() }
            }
        }
    }

    if (showDonateDialog) {
        if (BuildConfig.IS_GOOGLE_PLAY) {
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
