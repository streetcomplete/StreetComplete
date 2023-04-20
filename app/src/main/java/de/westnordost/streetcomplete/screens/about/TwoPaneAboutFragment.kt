package de.westnordost.streetcomplete.screens.about

import androidx.preference.PreferenceFragmentCompat
import de.westnordost.streetcomplete.screens.TwoPaneHeaderFragment

/** Shows the about screen lists and details in a two pane layout. */
class TwoPaneAboutFragment : TwoPaneHeaderFragment() {

    override fun onCreatePreferenceHeader(): PreferenceFragmentCompat {
        return AboutFragment()
    }
}
