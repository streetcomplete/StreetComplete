package de.westnordost.streetcomplete.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.getYamlObject
import kotlinx.android.synthetic.main.fragment_credits.*
import org.sufficientlysecure.htmltextview.HtmlTextView

/** Shows the credits of this app */
class CreditsFragment : Fragment(R.layout.fragment_credits) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addContributorsTo(readMainContributors(), mainCredits)
        addContributorsTo(readProjectsContributors(), projectsCredits)
        addContributorsTo(readCodeContributors(), codeCredits)
        addContributorsTo(readArtContributors(), artCredits)

        val inflater = LayoutInflater.from(view.context)
        for ((language, translators) in readTranslators()) {
            val item = inflater.inflate(R.layout.row_credits_translators, translationCredits, false)
            (item.findViewById<View>(R.id.language) as TextView).text = language
            (item.findViewById<View>(R.id.contributors) as TextView).text = translators
            translationCredits.addView(item)
        }

        val translationCreditsMore = view.findViewById<HtmlTextView>(R.id.translationCreditsMore)
        translationCreditsMore.setHtml(getString(R.string.credits_translations))
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
        textView.setTextAppearance(activity, R.style.TextAppearance_Body)
        textView.setHtml("<ul>$items</ul>")
        view.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    private fun readMainContributors() = resources.getYamlObject<List<String>>(R.raw.credits_main)

    private fun readProjectsContributors() = resources.getYamlObject<List<String>>(R.raw.credits_projects)

    private fun readCodeContributors() =
        resources.getYamlObject<List<String>>(R.raw.credits_code) + getString(R.string.credits_and_more)

    private fun readArtContributors() = resources.getYamlObject<List<String>>(R.raw.credits_art)

    private fun readTranslators() =
        resources.getYamlObject<LinkedHashMap<String, String>>(R.raw.credits_translations)
}
