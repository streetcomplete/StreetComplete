package de.westnordost.streetcomplete.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentCreditsBinding
import de.westnordost.streetcomplete.databinding.RowCreditsTranslatorsBinding
import de.westnordost.streetcomplete.ktx.getYamlObject
import de.westnordost.streetcomplete.ktx.getYamlStringList
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.Locale

/** Shows the credits of this app */
class CreditsFragment : Fragment(R.layout.fragment_credits) {

    private val binding by viewBinding(FragmentCreditsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleScope.launch {
            addContributorsTo(readMainContributors(), binding.mainCredits)
            addContributorsTo(readProjectsContributors(), binding.projectsCredits)
            addContributorsTo(readArtContributors(), binding.artCredits)
            addContributorsTo(readCodeContributors(), binding.codeCredits)

            val inflater = LayoutInflater.from(view.context)
            for ((language, translators) in readTranslators()) {
                val itemBinding = RowCreditsTranslatorsBinding.inflate(inflater, binding.translationCredits, false)
                itemBinding.language.text = language
                itemBinding.contributors.text = translators
                binding.translationCredits.addView(itemBinding.root)
            }
        }

        binding.authorText.setHtml("Tobias Zwick (<a href=\"https://github.com/westnordost\">westnordost</a>)")

        binding.contributorMore.setHtml(getString(R.string.credits_contributors))
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_authors)
    }

    private fun addContributorsTo(contributors: List<String>, view: ViewGroup) {
        val items = contributors.joinToString("") { "<li>$it</li>" }
        val textView = HtmlTextView(activity)
        TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Body)
        textView.setTextIsSelectable(true)
        textView.setHtml("<ul>$items</ul>")
        view.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    private suspend fun readMainContributors() = withContext(Dispatchers.IO) {
        resources.getYamlStringList(R.raw.credits_main).map(::withLinkToGithubAccount)
    }

    private suspend fun readProjectsContributors() = withContext(Dispatchers.IO) {
        resources.getYamlStringList(R.raw.credits_projects)
    }

    private suspend fun readCodeContributors() = withContext(Dispatchers.IO) {
        resources.getYamlStringList(R.raw.credits_code).map(::withLinkToGithubAccount) +
            getString(R.string.credits_and_more)
    }

    private suspend fun readArtContributors() = withContext(Dispatchers.IO) {
        resources.getYamlStringList(R.raw.credits_art)
    }

    private suspend fun readTranslators() = withContext(Dispatchers.IO) {
        val intMapSerializer = MapSerializer(String.serializer(), Int.serializer())
        val serializer = MapSerializer(String.serializer(), intMapSerializer)
        val map = resources.getYamlObject(serializer, R.raw.credits_translators).toDeepMutableMap()

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

private fun withLinkToGithubAccount(contributor: String): String {
    val regex = Regex("(.*?)\\s?(?:\\((.+)\\))?")
    val match = regex.matchEntire(contributor)!!
    val name = match.groupValues[1]
    val githubName = match.groupValues[2]
    return if (githubName.isEmpty()) {
        "<a href=\"https://github.com/$name\">$name</a>"
    } else {
        "$name (<a href=\"https://github.com/$githubName\">$githubName</a>)"
    }
}

private fun Map<String, Map<String, Int>>.toDeepMutableMap(): MutableMap<String, MutableMap<String, Int>> =
    entries.associate { it.key to it.value.toMutableMap() }.toMutableMap()
