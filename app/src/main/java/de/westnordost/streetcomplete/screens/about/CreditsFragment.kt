package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentCreditsBinding
import de.westnordost.streetcomplete.databinding.RowCreditsTranslatorsBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.setHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Shows the credits of this app */
class CreditsFragment : TwoPaneDetailFragment(R.layout.fragment_credits), HasTitle {

    private val binding by viewBinding(FragmentCreditsBinding::bind)
    private val viewModel by viewModel<CreditsViewModel>()

    override val title: String get() = getString(R.string.about_title_authors)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.credits) { credits ->
            if (credits == null) return@observe

            val mainContributorsWithLinks = credits.mainContributors.map { it.toTextWithLink() }
            val codeContributorsWithLinks = credits.codeContributors.map { it.toTextWithLink() }

            binding.authorText.setHtml(mainContributorsWithLinks.first())
            addContributorsTo(mainContributorsWithLinks.drop(1), binding.mainCredits)
            addContributorsTo(credits.projectsContributors, binding.projectsCredits)
            addContributorsTo(credits.artContributors, binding.artCredits)
            addContributorsTo(codeContributorsWithLinks + "…", binding.codeCredits)

            val translatorsByDisplayLanguage = credits.translators
                .map { Locale.forLanguageTag(it.key).displayLanguage to it.value }
                .sortedBy { it.first }

            val inflater = LayoutInflater.from(view.context)
            for ((language, translators) in translatorsByDisplayLanguage) {
                val itemBinding = RowCreditsTranslatorsBinding.inflate(inflater, binding.translationCredits, false)
                itemBinding.language.text = language
                itemBinding.contributors.text = translators.joinToString(", ")
                binding.translationCredits.addView(itemBinding.root)
            }
        }
        binding.contributorMore.setHtml(getString(R.string.credits_contributors))
    }

    private fun addContributorsTo(contributors: List<String>, view: ViewGroup) {
        view.removeAllViews()
        val items = contributors.joinToString("") { "<li>$it</li>" }
        val textView = TextView(activity)
        TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Body)
        textView.setTextIsSelectable(true)
        textView.setHtml("<ul>$items</ul>")
        view.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }
}

private fun Contributor.toTextWithLink(): String = when (githubUsername) {
    null -> name
    name -> "<a href=\"$githubLink\">$githubUsername</a>"
    else -> "$name (<a href=\"$githubLink\">$githubUsername</a>)"
}
