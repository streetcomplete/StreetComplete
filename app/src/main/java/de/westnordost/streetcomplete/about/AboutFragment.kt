package de.westnordost.streetcomplete.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.R

class AboutFragment : PreferenceFragmentCompat() {

    private val fragmentActivity: FragmentContainerActivity?
        get() = activity as FragmentContainerActivity?


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)

        findPreference("version").summary = BuildConfig.VERSION_NAME

        findPreference("license").setOnPreferenceClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html")
            )
            startActivity(browserIntent)
            true
        }

        findPreference("authors").setOnPreferenceClickListener {
            fragmentActivity?.setCurrentFragment(CreditsFragment())
            true
        }

        findPreference("privacy").setOnPreferenceClickListener {
            val f = ShowHtmlFragment.create(
                resources.getString(R.string.privacy_html) +
                getString(R.string.privacy_html_tileserver) +
                getString(R.string.privacy_html_third_party_quest_sources) +
                getString(R.string.privacy_html_image_upload2),
                R.string.about_title_privacy_statement
            )
            fragmentActivity?.setCurrentFragment(f)
            true
        }

        findPreference("repository").setOnPreferenceClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/westnordost/StreetComplete/")
            )
            startActivity(browserIntent)
            true
        }

        findPreference("report_error").setOnPreferenceClickListener {
	        val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/westnordost/StreetComplete/issues/")
            )
            startActivity(browserIntent)
            true
        }

        findPreference("email_feedback").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("osm@westnordost.de"))
            intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Feedback")
            if (activity?.let { intent.resolveActivity(it.packageManager) } == null) {
	            return@setOnPreferenceClickListener false
            }
            startActivity(intent)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.action_about)
    }
}
