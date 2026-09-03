package de.westnordost.streetcomplete.screens.about

object IosAppStoreInfo : AppStoreInfo {
    override fun getRatingUri(): String =
        "https://apps.apple.com/app/id6808344816?action=write-review"

    override fun disallowsInAppDonationLinks(): Boolean =
        true
}
