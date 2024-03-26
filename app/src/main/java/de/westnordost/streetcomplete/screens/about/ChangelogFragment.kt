package de.westnordost.streetcomplete.screens.about

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogWhatsNewBinding
import de.westnordost.streetcomplete.databinding.FragmentChangelogBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.getRawTextFile
import de.westnordost.streetcomplete.util.ktx.indicesOf
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.pxToSp
import de.westnordost.streetcomplete.util.ktx.setHtmlBody
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** Shows the full changelog */
class ChangelogFragment : TwoPaneDetailFragment(R.layout.fragment_changelog), HasTitle {

    private val binding by viewBinding(FragmentChangelogBinding::bind)

    override val title: String get() = getString(R.string.about_title_changelog)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleScope.launch {
            val changelog = readChangelog(resources)
            binding.webView.setHtmlBody(changelog)
        }
    }
}

/** A dialog that shows the changelog */
class WhatsNewDialog(context: Context, sinceVersion: String) : AlertDialog(context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val binding = DialogWhatsNewBinding.inflate(LayoutInflater.from(context))

        setTitle(R.string.title_whats_new)
        setView(binding.root)
        setButton(DialogInterface.BUTTON_POSITIVE, context.resources.getText(android.R.string.ok), null, null)

        scope.launch {
            val fullChangelog = readChangelog(context.resources)
            var sinceVersionIndex = fullChangelog.indexOf("<h2>$sinceVersion</h2>")
            if (sinceVersionIndex == -1) {
                // if version not found, just show the last one
                sinceVersionIndex = fullChangelog.indicesOf("<h2>").elementAt(1)
            }
            val changelog = fullChangelog.substring(0, sinceVersionIndex)

            binding.webView.setHtmlBody(changelog)
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }
}

private suspend fun readChangelog(resources: Resources): String = withContext(Dispatchers.IO) {
    resources.getRawTextFile(R.raw.changelog)
}
