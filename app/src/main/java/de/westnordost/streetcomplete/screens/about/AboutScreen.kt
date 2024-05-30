package de.westnordost.streetcomplete.screens.about

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.ApplicationConstants.COPYRIGHT_YEARS
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
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
    val showDonateDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.action_about2)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )

        PreferenceItem(
            title = stringResource(R.string.about_title_changelog),
            onClick = { onClickChangelog() },
            summary = stringResource(R.string.about_summary_current_version, "v" + BuildConfig.VERSION_NAME),
            icon = painterResource(R.drawable.ic_chevron_next_24dp)
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_authors),
            onClick = { onClickCredits() },
            summary = stringResource(R.string.about_summary_authors, COPYRIGHT_YEARS),
            icon = painterResource(R.drawable.ic_chevron_next_24dp)
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_license),
            onClick = { context.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
            summary = stringResource(R.string.about_summary_license),
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_faq),
            onClick = { context.openUri("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ") },
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_privacy_statement),
            onClick = { onClickPrivacyStatement() },
            summary = stringResource(R.string.about_summary_privacy_statement),
            icon = painterResource(R.drawable.ic_chevron_next_24dp),
        )

        PreferenceCategoryItem(stringResource(R.string.about_category_contribute))

        PreferenceItem(
            title = stringResource(R.string.about_title_donate),
            onClick = { showDonateDialog.value = true },
            summary = stringResource(R.string.about_summary_donate),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_translate),
            onClick = { context.openUri("https://poeditor.com/join/project/IE4GC127Ki") },
            summary = stringResource(
                R.string.about_description_translate,
                Locale.getDefault().displayLanguage,
                integerResource(R.integer.translation_completeness)
            ),
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_repository),
            onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete") },
            summary = stringResource(R.string.about_summary_repository),
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )

        PreferenceCategoryItem(stringResource(R.string.about_category_feedback))

        if (context.isInstalledViaGooglePlay()) {
            PreferenceItem(
                title = stringResource(R.string.about_title_rate),
                onClick = { context.openGooglePlayStorePage() },
                summary = stringResource(R.string.about_summary_rate),
                icon = painterResource(R.drawable.ic_open_in_browser_24dp),
            )
        }
        PreferenceItem(
            title = stringResource(R.string.about_title_report_error),
            onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete/issues") },
            summary = stringResource(R.string.about_summary_repository),
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_feedback),
            onClick = { context.openUri("https://github.com/streetcomplete/StreetComplete/discussions") },
            summary = stringResource(R.string.about_summary_repository),
            icon = painterResource(R.drawable.ic_open_in_browser_24dp),
        )
        PreferenceItem(
            title = stringResource(R.string.about_title_show_logs),
            onClick = { onClickLogs() },
            summary = stringResource(R.string.about_summary_logs),
            icon = painterResource(R.drawable.ic_chevron_next_24dp),
        )
    }

    if (showDonateDialog.value) {
        if (BuildConfig.IS_GOOGLE_PLAY) {
            DonationsGooglePlayDialog { showDonateDialog.value = false }
        } else {
            DonationsDialog(
                onDismissRequest = { showDonateDialog.value = false },
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
    AboutScreen({}, {}, {}, {}, {},)
}
