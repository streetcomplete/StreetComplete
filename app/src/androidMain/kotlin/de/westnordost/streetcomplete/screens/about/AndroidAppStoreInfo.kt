package de.westnordost.streetcomplete.screens.about

import android.content.Context

class AndroidAppStoreInfo(
    private val context: Context,
) : AppStoreInfo {
    override fun getRatingUri(): String? =
        if (context.isInstalledViaGooglePlay()) {
            "https://play.google.com/store/apps/details?id=${context.packageName}"
        } else {
            null
        }

    override fun disallowsInAppDonationLinks(): Boolean =
        context.isInstalledViaGooglePlay()

    private fun Context.isInstalledViaGooglePlay(): Boolean =
        applicationContext.packageManager
            .getInstallSourceInfo(applicationContext.packageName)
            .installingPackageName == "com.android.vending"
}
