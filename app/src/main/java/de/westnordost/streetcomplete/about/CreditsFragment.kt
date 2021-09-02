package de.westnordost.streetcomplete.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.getYamlObject
import kotlinx.android.synthetic.main.fragment_credits.*
import kotlinx.coroutines.*
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.*
import kotlin.collections.LinkedHashMap

/** Shows the credits of this app */
class CreditsFragment : Fragment(R.layout.fragment_credits) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            addContributorsTo(readMainContributors(), mainCredits)
            addContributorsTo(readProjectsContributors(), projectsCredits)
            addContributorsTo(readArtContributors(), artCredits)
            addContributorsTo(readCodeContributors(), codeCredits)

            val inflater = LayoutInflater.from(view.context)
            for ((language, translators) in readTranslators()) {
                val item = inflater.inflate(R.layout.row_credits_translators, translationCredits, false)
                (item.findViewById<View>(R.id.language) as TextView).text = language
                (item.findViewById<View>(R.id.contributors) as TextView).text = translators
                translationCredits.addView(item)
            }
        }

        authorText.setHtml("Tobias Zwick (<a href=\"https://github.com/westnordost\">westnordost</a>)")

        val contributorMore = view.findViewById<HtmlTextView>(R.id.contributorMore)
        contributorMore.setHtml(getString(R.string.credits_contributors))
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_authors)
    }

    private fun addContributorsTo(contributors: List<String>, view: ViewGroup) {
        val items = contributors.map { "<li>$it</li>" }.joinToString("")
        val textView = HtmlTextView(activity)
        TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Body)
        textView.setTextIsSelectable(true)
        textView.setHtml("<ul>$items</ul>")
        view.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    private suspend fun readMainContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_main).map(::withLinkToGithubAccount)
    }

    private suspend fun readProjectsContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_projects)
    }

    private suspend fun readCodeContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_code).map(::withLinkToGithubAccount) +
            getString(R.string.credits_and_more)
    }

    private suspend fun readArtContributors() = withContext(Dispatchers.IO) {
        resources.getYamlObject<List<String>>(R.raw.credits_art)
    }

    private suspend fun readTranslators() = withContext(Dispatchers.IO) {
        val map = resources.getYamlObject<LinkedHashMap<String, LinkedHashMap<String, String>>>(R.raw.credits_translators)

        // skip those translators who contributed less than 2% of the translation
        for (contributors in map.values) {
            val totalTranslated = contributors.values.sumOf { it.toInt() }
            val removedAnyone = contributors.values.removeAll { 100 * it.toInt() / totalTranslated < 2 }
            if (removedAnyone) {
                contributors[""] = "1"
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
                .sortedByDescending { it.value.toInt() }
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
