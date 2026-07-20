package de.westnordost.streetcomplete.screens.about

object IosAppStoreInfo : AppStoreInfo {
    override fun getRatingUri(): String =
        "https://apps.apple.com/app/id${TODO("insert StreetComplete app id")}?action=write-review"

    override fun disallowsInAppDonationLinks(): Boolean =
        true
}
