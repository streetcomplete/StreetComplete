package de.westnordost.streetcomplete.screens.about

import android.content.Context

class AndroidAppStoreInfo(
    private val context: Context,
) : AppStoreInfo {
    override fun getRatingUri(): String? =
        if (context.isInstalledViaGooglePlay() == true) {
            "https://play.google.com/store/apps/details?id=${context.packageName}"
        } else {
            null
        }

    override fun disallowsInAppDonationLinks(): Boolean =
        context.isInstalledViaGooglePlay() != false

    private fun Context.isInstalledViaGooglePlay(): Boolean? =
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            applicationContext.packageManager
                .getInstallSourceInfo(applicationContext.packageName)
                .installingPackageName == "com.android.vending"
        } else {
            null
        }
}
