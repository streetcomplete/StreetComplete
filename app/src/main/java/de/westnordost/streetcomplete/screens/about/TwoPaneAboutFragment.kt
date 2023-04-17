package de.westnordost.streetcomplete.screens.about

import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceHeaderFragmentCompat

/** Shows the about screen lists and details in a two pane layout. */
class TwoPaneAboutFragment : PreferenceHeaderFragmentCompat() {

    override fun onCreatePreferenceHeader(): PreferenceFragmentCompat {
        return AboutFragment()
    }
}
