package de.westnordost.streetcomplete.screens.about

/** Provides information pertaining to the app store through which this app was installed. */
interface AppStoreInfo {
    /** Get URI for rating this app in the app store. Returns null if the app hasn't been installed
     *  through a store that allows rating the app (e.g. F-Droid, just installed the APK, ...) */
    fun getRatingUri(): String?

    /** Whether this app store allows displaying links to donation platforms like Patreon etc.
     *  within the app */
    fun disallowsInAppDonationLinks(): Boolean
}
