package de.westnordost.streetcomplete.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentShowHtmlBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.map.VectorTileProvider
import javax.inject.Inject

/** Shows the privacy statement */
class PrivacyStatementFragment : Fragment(R.layout.fragment_show_html) {

    @Inject internal lateinit var vectorTileProvider: VectorTileProvider

    private val binding by viewBinding(FragmentShowHtmlBinding::bind)

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.text.setHtml(
            getString(R.string.privacy_html) +
            vectorTileProvider.baseTileSource.let {
                getString(R.string.privacy_html_tileserver2, it.title, it.privacyStatementLink)
            } +
            getString(R.string.privacy_html_statistics) +
            getString(R.string.privacy_html_third_party_quest_sources) +
            getString(R.string.privacy_html_image_upload2)
        )
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_privacy_statement)
    }
}
