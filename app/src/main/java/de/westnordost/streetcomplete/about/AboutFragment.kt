package de.westnordost.streetcomplete.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.cell_labeled_icon_select_right.view.*


class AboutFragment : PreferenceFragmentCompat() {

    private val fragmentActivity: FragmentContainerActivity?
        get() = activity as FragmentContainerActivity?


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)

        findPreference<Preference>("version")?.summary = getString(R.string.about_summary_current_version, "v" + BuildConfig.VERSION_NAME)
        findPreference<Preference>("version")?.setOnPreferenceClickListener {
            fragmentActivity?.setCurrentFragment(ChangelogFragment())
            true
        }

        findPreference<Preference>("license")?.setOnPreferenceClickListener {
            openUrl("https://www.gnu.org/licenses/gpl-3.0.html")
        }

        findPreference<Preference>("authors")?.setOnPreferenceClickListener {
            fragmentActivity?.setCurrentFragment(CreditsFragment())
            true
        }

        findPreference<Preference>("privacy")?.setOnPreferenceClickListener {
            val f = ShowHtmlFragment.create(
                getString(R.string.privacy_html) +
                getString(R.string.privacy_html_tileserver) +
                getString(R.string.privacy_html_third_party_quest_sources) +
                getString(R.string.privacy_html_image_upload2),
                R.string.about_title_privacy_statement
            )
            fragmentActivity?.setCurrentFragment(f)
            true
        }

        findPreference<Preference>("repository")?.setOnPreferenceClickListener {
            openUrl("https://github.com/westnordost/StreetComplete/")
        }

        findPreference<Preference>("report_error")?.setOnPreferenceClickListener {
            openUrl("https://github.com/westnordost/StreetComplete/issues/")
        }

        findPreference<Preference>("email_feedback")?.setOnPreferenceClickListener {
            sendFeedbackEmail()
        }

        findPreference<Preference>("rate")?.isVisible = isInstalledViaGooglePlay()
        findPreference<Preference>("rate")?.setOnPreferenceClickListener {
            openGooglePlayStorePage()
        }

        findPreference<Preference>("donate")?.setOnPreferenceClickListener {
            showDonateDialog()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.action_about)
    }

    private fun isInstalledViaGooglePlay(): Boolean {
        val appCtx = context?.applicationContext ?: return false
        val installerPackageName = appCtx.packageManager.getInstallerPackageName(appCtx.packageName)
        return installerPackageName == "com.android.vending"
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return tryStartActivity(intent)
    }

    private fun sendFeedbackEmail(): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("osm@westnordost.de"))
        intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Feedback")
        return tryStartActivity(intent)
    }

    private fun openGooglePlayStorePage(): Boolean {
        val appPackageName = context?.applicationContext?.packageName ?: return false
        return openUrl("market://details?id=$appPackageName")
    }

    private fun tryStartActivity(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun showDonateDialog() {
        val ctx = context ?: return

        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_donate, null)
        val listView = view.findViewById<RecyclerView>(R.id.donateList)
        listView.adapter = DonationPlatformAdapter(DonationPlatform.values().asList())

        AlertDialog.Builder(ctx)
                .setTitle(R.string.about_title_donate)
                .setView(view)
                .show()
    }

    private inner class DonationPlatformAdapter(list: List<DonationPlatform>): ListAdapter<DonationPlatform>(list) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_labeled_icon_select_right, parent, false))

        inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<DonationPlatform>(itemView) {
            override fun onBind(with: DonationPlatform) {
                itemView.imageView.setImageResource(with.iconId)
                itemView.textView.text = with.title
                itemView.setOnClickListener { openUrl(with.url) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    itemView.textView.setTextAppearance(R.style.TextAppearance_Title)
                }
            }
        }
    }
}

private enum class DonationPlatform(val title: String, @DrawableRes val iconId: Int, val url: String) {
    GITHUB("GitHub Sponsors", R.drawable.ic_github, "https://github.com/sponsors/westnordost"),
    LIBERAPAY("Liberapay", R.drawable.ic_liberapay, "https://liberapay.com/westnordost"),
    PATREON("Patreon", R.drawable.ic_patreon, "https://patreon.com/westnordost")
}
