package de.westnordost.streetcomplete.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R

class ShowHtmlFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_show_html, container, false)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = Html.fromHtml(arguments!!.getString(TEXT))
        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(arguments!!.getInt(TITLE_STRING_RESOURCE_ID))
    }

    companion object {
        private const val TEXT = "text"
        private const val TITLE_STRING_RESOURCE_ID = "title_string_id"

        fun create(text: String, titleStringId: Int): ShowHtmlFragment {
            val result = ShowHtmlFragment()
            result.arguments = bundleOf(
                TEXT to text,
                TITLE_STRING_RESOURCE_ID to titleStringId
            )
            return result
        }
    }
}
