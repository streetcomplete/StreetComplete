package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentShowHtmlBinding
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.setHtml
import org.koin.android.ext.android.inject

/** Shows the privacy statement */
class PrivacyStatementFragment : Fragment(R.layout.fragment_show_html) {

    private val vectorTileProvider: VectorTileProvider by inject()

    private val binding by viewBinding(FragmentShowHtmlBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textView.setHtml(
            getString(R.string.privacy_html) +
            getString(R.string.privacy_html_tileserver2, vectorTileProvider.title, vectorTileProvider.privacyStatementLink) +
            getString(R.string.privacy_html_statistics) +
            getString(R.string.privacy_html_third_party_quest_sources) +
            getString(R.string.privacy_html_image_upload2))
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_privacy_statement)
    }
}
