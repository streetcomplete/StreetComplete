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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.util.ktx.openUri
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

    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.action_about2)) },
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
                    onClick = { context.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
                ) {
                    Text("GPLv3")
                    OpenInBrowserIcon()
                }

                Preference(
                    name = stringResource(R.string.about_title_privacy_statement),
                    onClick = { onClickPrivacyStatement() },
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(R.string.about_title_faq),
                    onClick = { context.openUri("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ") },
                ) { OpenInBrowserIcon() }
            }

            PreferenceCategory(stringResource(R.string.about_category_contribute)) {

                Preference(
                    name = stringResource(R.string.about_title_donate),
                    onClick = { showDonateDialog = true },
                    description = stringResource(R.string.about_summary_donate),
                )

                Preference(
                    name = stringResource(R.string.about_title_translate),
                    onClick = { context.openUri("https://poeditor.com/join/project/IE4GC127Ki") },
                    description = stringResource(
                        R.string.about_description_translate,
                        Locale.getDefault().displayLanguage,
                        integerResource(R.integer.translation_completeness)
                    )
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_repository),
                    onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete") },
                ) { OpenInBrowserIcon() }
            }

            PreferenceCategory(stringResource(R.string.about_category_feedback)) {

                if (context.isInstalledViaGooglePlay()) {
                    Preference(
                        name = stringResource(R.string.about_title_rate),
                        onClick = { context.openGooglePlayStorePage() },
                    ) { OpenInBrowserIcon() }
                }

                Preference(
                    name = stringResource(R.string.about_title_report_error),
                    onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete/issues") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_feedback),
                    onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete/discussions") },
                ) { OpenInBrowserIcon() }

                Preference(
                    name = stringResource(R.string.about_title_show_logs),
                    onClick = { onClickLogs() },
                    description = stringResource(R.string.about_summary_logs),
                ) { NextScreenIcon() }
            }
        }
    }

    if (showDonateDialog) {
        if (BuildConfig.IS_GOOGLE_PLAY) {
            DonationsGooglePlayDialog { showDonateDialog = false }
        } else {
            DonationsDialog(
                onDismissRequest = { showDonateDialog = false },
                onClickLink = { context.openUri(it) }
            )
        }
    }
}

private fun Context.isInstalledViaGooglePlay(): Boolean =
    applicationContext.packageManager.getInstallerPackageName(applicationContext.packageName) == "com.android.vending"

private fun Context.openGooglePlayStorePage() {
    openUri("market://details?id=$packageName")
}

@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen({}, {}, {}, {}, {})
}
