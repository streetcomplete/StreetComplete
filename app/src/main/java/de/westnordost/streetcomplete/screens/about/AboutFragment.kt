package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.TextViewCompat
import androidx.preference.Preference
import de.westnordost.streetcomplete.ApplicationConstants.COPYRIGHT_YEARS
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.CellLabeledIconSelectRightBinding
import de.westnordost.streetcomplete.databinding.DialogDonateBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneListFragment
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import de.westnordost.streetcomplete.view.ListAdapter
import java.util.Locale

/** Shows the about screen list. */
class AboutFragment : TwoPaneListFragment(), HasTitle {

    override val title: String get() = getString(R.string.action_about2) + " SCEE"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)

        findPreference<Preference>("version")?.summary =
            getString(R.string.about_summary_current_version, "v" + BuildConfig.VERSION_NAME)

        findPreference<Preference>("authors")?.summary =
            getString(R.string.about_summary_authors, COPYRIGHT_YEARS)

        findPreference<Preference>("license")
            ?.setClickOpensUrl("https://www.gnu.org/licenses/gpl-3.0.html")
        findPreference<Preference>("repository")
            ?.setClickOpensUrl("https://github.com/streetcomplete/StreetComplete")
        findPreference<Preference>("faq")
            ?.setClickOpensUrl("https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ")
        findPreference<Preference>("fork_repository")
            ?.setClickOpensUrl("https://github.com/Helium314/SCEE/")

        findPreference<Preference>("translate")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(resources.getString(
                    R.string.about_description_translate,
                    Locale.getDefault().displayLanguage,
                    resources.getInteger(R.integer.translation_completeness)
                ))
                .setPositiveButton("StreetComplete") { _, _ -> openUri("https://poeditor.com/join/project/IE4GC127Ki")}
                .setNegativeButton("SCEE") { _, _ -> openUri("https://translate.codeberg.org/projects/scee/")}
                .show()
            true
        }

        findPreference<Preference>("report_error")
            ?.setClickOpensUrl("https://github.com/Helium314/SCEE/issues/")
        findPreference<Preference>("give_feedback")
            ?.setClickOpensUrl("https://github.com/Helium314/SCEE/discussions/")

        val ratePreference = findPreference<Preference>("rate")
        ratePreference?.isVisible = isInstalledViaGooglePlay()
        ratePreference?.setOnPreferenceClickListener {
            openGooglePlayStorePage()
            true
        }

        findPreference<Preference>("donate")?.setOnPreferenceClickListener {
            showDonateDialog()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbarTitleAndIcon(view.findViewById(R.id.toolbar))
    }

    private fun isInstalledViaGooglePlay(): Boolean {
        val appCtx = context?.applicationContext ?: return false
        val installerPackageName = appCtx.packageManager.getInstallerPackageName(appCtx.packageName)
        return installerPackageName == "com.android.vending"
    }

    private fun Preference.setClickOpensUrl(url: String) {
        setOnPreferenceClickListener {
            openUri(url)
            true
        }
    }

    private fun openGooglePlayStorePage() {
        val appPackageName = context?.packageName ?: return
        openUri("market://details?id=$appPackageName")
    }

    private fun showDonateDialog() {
        val ctx = context ?: return

        if (!BuildConfig.IS_GOOGLE_PLAY) {
            val dialogBinding = DialogDonateBinding.inflate(layoutInflater)
            dialogBinding.donateList.adapter = DonationPlatformAdapter(DonationPlatform.entries)
            AlertDialog.Builder(ctx)
                .setView(dialogBinding.root)
                .show()
        } else {
            AlertDialog.Builder(ctx)
                .setMessage(R.string.about_description_donate_google_play3)
                .show()
        }
    }

    private inner class DonationPlatformAdapter(list: List<DonationPlatform>) :
        ListAdapter<DonationPlatform>(list) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(CellLabeledIconSelectRightBinding.inflate(layoutInflater, parent, false))

        inner class ViewHolder(val binding: CellLabeledIconSelectRightBinding) :
            ListAdapter.ViewHolder<DonationPlatform>(binding) {
            override fun onBind(with: DonationPlatform) {
                binding.imageView.setImageResource(with.iconId)
                binding.textView.text = with.title
                binding.root.setOnClickListener { openUri(with.url) }
                TextViewCompat.setTextAppearance(binding.textView, R.style.TextAppearance_TitleLarge)
            }
        }
    }
}

private enum class DonationPlatform(
    val title: String,
    @DrawableRes val iconId: Int,
    val url: String
) {
    LIBERAPAY2("Liberapay (Helium314)", R.drawable.ic_liberapay, "https://liberapay.com/helium314"),
    GITHUB("GitHub Sponsors (westnordost)", R.drawable.ic_github, "https://github.com/sponsors/westnordost"),
    LIBERAPAY("Liberapay (westnordost)", R.drawable.ic_liberapay, "https://liberapay.com/westnordost"),
    PATREON("Patreon (westnordost)", R.drawable.ic_patreon, "https://patreon.com/westnordost")
}
