package de.westnordost.streetcomplete.settings

import android.os.Bundle
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.about.AboutFragment
import de.westnordost.streetcomplete.about.ChangelogFragment
import de.westnordost.streetcomplete.about.CreditsFragment
import de.westnordost.streetcomplete.about.ShowHtmlFragment
import de.westnordost.streetcomplete.map.VectorTileProvider
import javax.inject.Inject

class AboutActivity : FragmentContainerActivity(), AboutFragment.Listener
{
    @Inject internal lateinit var vectorTileProvider: VectorTileProvider

    init {
        Injector.applicationComponent.inject(this)
    }

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
        pushMainFragment(ShowHtmlFragment.create(
            getString(R.string.privacy_html) +
            getString(R.string.privacy_html_tileserver2, vectorTileProvider.title, vectorTileProvider.privacyStatementLink) +
            getString(R.string.privacy_html_statistics) +
            getString(R.string.privacy_html_third_party_quest_sources) +
            getString(R.string.privacy_html_image_upload2),
            R.string.about_title_privacy_statement
        ))
    }
}
