package de.westnordost.streetcomplete.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import org.sufficientlysecure.htmltextview.HtmlTextView

class ShowHtmlFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_show_html, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<HtmlTextView>(R.id.text)
        textView.setHtml(arguments!!.getString(TEXT)!!)
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(arguments!!.getInt(TITLE_STRING_RESOURCE_ID))
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
