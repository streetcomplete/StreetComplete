package de.westnordost.streetcomplete.screens.about

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.html.HtmlElementNode
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlTextNode
import de.westnordost.streetcomplete.util.html.parseHtml
import de.westnordost.streetcomplete.util.ktx.getRawTextFile
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

abstract class ChangelogViewModel : ViewModel() {
    /* version name -> html */
    abstract val changelog: StateFlow<Map<String, List<HtmlNode>>?>
}

class ChangelogViewModelImpl(resources: Resources) : ChangelogViewModel() {
    override val changelog = MutableStateFlow<Map<String, List<HtmlNode>>?>(null)

    init {
        launch {
            changelog.value = getChangelog(resources)
        }
    }

    private suspend fun getChangelog(resources: Resources): Map<String, List<HtmlNode>> {
        val text = withContext(Dispatchers.IO) { resources.getRawTextFile(R.raw.changelog) }
        val html = parseHtml(text)
        var currentVersion: String? = null
        var versionHtml = ArrayList<HtmlNode>()
        val result = LinkedHashMap<String, List<HtmlNode>>()
        for (node in html) {
            if (node is HtmlElementNode && node.tag == "h2") {
                if (currentVersion != null) {
                    result[currentVersion] = versionHtml
                    versionHtml = ArrayList()
                }
                currentVersion = (node.nodes.first() as HtmlTextNode).text.trim()
            } else {
                versionHtml.add(node)
            }
        }
        return result
    }
}
