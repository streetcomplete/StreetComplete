package de.westnordost.streetcomplete.about

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import org.sufficientlysecure.htmltextview.HtmlTextView

/** Shows a simple html without using a WebView */
class ShowHtmlFragment : Fragment(R.layout.fragment_show_html) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<HtmlTextView>(R.id.text)
        textView.setHtml(requireArguments().getString(TEXT)!!)
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(requireArguments().getInt(TITLE_STRING_RESOURCE_ID))
    }

    companion object {
        private const val TEXT = "text"
        private const val TITLE_STRING_RESOURCE_ID = "title_string_id"

        fun create(text: String, titleStringId: Int) = ShowHtmlFragment().apply {
            arguments = bundleOf(
                TEXT to text,
                TITLE_STRING_RESOURCE_ID to titleStringId
            )
        }
    }
}
