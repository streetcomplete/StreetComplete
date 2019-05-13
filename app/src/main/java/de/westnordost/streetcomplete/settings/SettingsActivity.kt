package de.westnordost.streetcomplete.settings

import android.os.Bundle

import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment

class SettingsActivity : FragmentContainerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false)) {
            OsmOAuthDialogFragment().show(supportFragmentManager, OsmOAuthDialogFragment.TAG)
        }
        intent.putExtra(EXTRA_FRAGMENT_CLASS, SettingsFragment::class.java.name)
    }

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.settings.launch_auth"
    }
}
