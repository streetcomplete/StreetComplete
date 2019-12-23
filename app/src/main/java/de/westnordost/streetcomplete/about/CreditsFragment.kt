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
import org.sufficientlysecure.htmltextview.HtmlTextView

class CreditsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_credits, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val contributorCredits = view.findViewById<LinearLayout>(R.id.contributorCredits)
        for (contributor in readContributors()) {
            val textView = TextView(activity)
            textView.text = contributor
            contributorCredits.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }

        val translationCredits = view.findViewById<LinearLayout>(R.id.translationCredits)
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

    private fun readContributors() =
        resources.getYamlObject<List<String>>(R.raw.credits_contributors) + getString(R.string.credits_and_more)

    private fun readTranslators() =
        resources.getYamlObject<LinkedHashMap<String, String>>(R.raw.credits_translations)
}
