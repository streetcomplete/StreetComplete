package de.westnordost.streetcomplete.screens.about

// TODO desktop: fill in once distributed via a desktop app store (Flathub, Microsoft Store, ...)
object DesktopAppStoreInfo : AppStoreInfo {
    override fun getRatingUri(): String? =
        null

    override fun disallowsInAppDonationLinks(): Boolean =
        false
}
