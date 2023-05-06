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
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.setHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.Locale

private typealias TranslationCreditMap = MutableMap<String, MutableMap<String, Int>>

/** Shows the credits of this app */
class CreditsFragment : TwoPaneDetailFragment(R.layout.fragment_credits), HasTitle {

    private val binding by viewBinding(FragmentCreditsBinding::bind)

    override val title: String get() = getString(R.string.about_title_authors)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleScope.launch {
            val mainContributors = readMainContributors()
            val mainContributorUsernames = mainContributors.map { it.githubUsername }
            val mainContributorLinks = mainContributors.map { it.toTextWithLink() }

            binding.authorText.setHtml(mainContributorLinks.first())
            addContributorsTo(mainContributorLinks.drop(1), binding.mainCredits)
            addContributorsTo(readProjectsContributors(), binding.projectsCredits)
            addContributorsTo(readArtContributors(), binding.artCredits)
            addContributorsTo(readCodeContributors(mainContributorUsernames), binding.codeCredits)

            val inflater = LayoutInflater.from(view.context)
            for ((language, translators) in readTranslators()) {
                val itemBinding = RowCreditsTranslatorsBinding.inflate(inflater, binding.translationCredits, false)
                itemBinding.language.text = language
                itemBinding.contributors.text = translators
                binding.translationCredits.addView(itemBinding.root)
            }
        }

        binding.contributorMore.setHtml(getString(R.string.credits_contributors))
    }

    private fun addContributorsTo(contributors: List<String>, view: ViewGroup) {
        val items = contributors.joinToString("") { "<li>$it</li>" }
        val textView = TextView(activity)
        TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Body)
        textView.setTextIsSelectable(true)
        textView.setHtml("<ul>$items</ul>")
        view.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    private suspend fun readMainContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<Contributor>>(R.raw.credits_main)
    }

    private suspend fun readProjectsContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_projects)
    }

    private suspend fun readCodeContributors(skipUsers: List<String?>) = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<Contributor>>(R.raw.credits_contributors)
            .filter { it.githubUsername !in skipUsers && it.score >= 50 }
            .sortedByDescending { it.score }
            .map { it.toTextWithLink() } + getString(R.string.credits_and_more)
    }

    private suspend fun readArtContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_art)
    }

    private suspend fun readTranslators() = withContext(Dispatchers.IO) {
        val map = resources.getYamlObject<TranslationCreditMap>(R.raw.credits_translators)

        // skip those translators who contributed less than 2% of the translation
        for (contributors in map.values) {
            val totalTranslated = contributors.values.sum()
            val removedAnyone = contributors.values.removeAll { 100 * it / totalTranslated < 2 }
            if (removedAnyone) {
                contributors[""] = 1
            }
        }
        // skip plain English. That's not a translation
        map.remove("en")

        val languageTagByName = map.keys.associateBy { tag ->
            val locale = Locale.forLanguageTag(tag)
            locale.getDisplayName(locale)
        }
        val namesSorted = languageTagByName.keys.toList().sorted()

        namesSorted.associateWith { name ->
            val contributionCountByName = map[languageTagByName[name]]!!
            contributionCountByName.entries
                .sortedByDescending { it.value }
                .joinToString(", ") { it.key }
                .replace(Regex(", $"), " " + getString(R.string.credits_and_more))
        }
    }
}

private val Contributor.score: Int get() =
    linesOfCodeChanged + linesOfInterfaceMarkupChanged / 5 + assetFilesChanged * 15

private fun Contributor.toTextWithLink(): String = when (githubUsername) {
    null -> name
    name -> "<a href=\"https://github.com/$githubUsername\">$githubUsername</a>"
    else -> "$name (<a href=\"https://github.com/$githubUsername\">$githubUsername</a>)"
}

@Serializable
private data class Contributor(
    val name: String,
    val githubUsername: String? = null,
    val linesOfCodeChanged: Int = 0,
    val linesOfInterfaceMarkupChanged: Int = 0,
    val assetFilesChanged: Int = 0
)
