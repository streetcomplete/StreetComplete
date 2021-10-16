package de.westnordost.streetcomplete.quests

interface RequireCountrySpecificResourcesListener {
    /** Called by a fragment on its parent activity (usually in onCreate), declaring that it needs
     *  to access country-specific resources.
     *
     *  The Android resource system is not designed to offer different resources depending on the
     *  country (code). But what it can do is to offer different resources for different
     *  "mobile country codes" which usually contains the code of your mobile phone network
     *  provider.
     *
     *  So, with this callback, a fragment can request its parent activity to override its resources
     *  to the given MCC code */
    fun onRequireCountrySpecificResources(mobileCountryCode: Int)
}
