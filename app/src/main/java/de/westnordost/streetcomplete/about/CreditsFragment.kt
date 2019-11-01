package de.westnordost.streetcomplete.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView

import com.esotericsoftware.yamlbeans.YamlReader

import java.io.InputStreamReader
import java.util.ArrayList

import de.westnordost.streetcomplete.R

class CreditsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_credits, container, false)

        val contributorCredits = view.findViewById<LinearLayout>(R.id.contributorCredits)
        for (contributor in readContributors()) {
            val textView = TextView(activity)
            textView.text = contributor
            contributorCredits.addView(textView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }

        val translationCredits = view.findViewById<LinearLayout>(R.id.translationCredits)
        for (translatorsByLanguage in readTranslators()) {
            val (language, translators) = translatorsByLanguage
            val item = inflater.inflate(R.layout.row_credits_translators, translationCredits, false)
            (item.findViewById<View>(R.id.language) as TextView).text = language
            (item.findViewById<View>(R.id.contributors) as TextView).text = translators
            translationCredits.addView(item)
        }

        val translationCreditsMore = view.findViewById<TextView>(R.id.translationCreditsMore)
        translationCreditsMore.movementMethod = LinkMovementMethod.getInstance()
        translationCreditsMore.text = Html.fromHtml(getString(R.string.credits_translations))
        val contributorMore = view.findViewById<TextView>(R.id.contributorMore)
        contributorMore.movementMethod = LinkMovementMethod.getInstance()
        contributorMore.text = Html.fromHtml(getString(R.string.credits_contributors))

        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_authors)
    }

    private fun readContributors(): List<String> {
        val inputStream = resources.openRawResource(R.raw.credits_contributors)
        val reader = YamlReader(InputStreamReader(inputStream))
        val result = ArrayList<String>(reader.read() as List<String>)
        result.add(getString(R.string.credits_and_more))
        return result
    }

    private fun readTranslators(): List<Pair<String, String>> {
        val inputStream = resources.openRawResource(R.raw.credits_translations)
        val reader = YamlReader(InputStreamReader(inputStream))
        val result = (reader.read() as Map<String, String>).toList()
        return result.sortedBy { e -> e.first }
    }
}
