package de.westnordost.streetcomplete.about

import android.os.Bundle
import de.westnordost.streetcomplete.FragmentContainerActivity

class AboutActivity : FragmentContainerActivity(), AboutFragment.Listener
{
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = AboutFragment()
        }
    }

    override fun onClickedChangelog() {
        pushMainFragment(ChangelogFragment())
    }

    override fun onClickedCredits() {
        pushMainFragment(CreditsFragment())
    }

    override fun onClickedPrivacyStatement() {
        pushMainFragment(PrivacyStatementFragment())
    }
}
