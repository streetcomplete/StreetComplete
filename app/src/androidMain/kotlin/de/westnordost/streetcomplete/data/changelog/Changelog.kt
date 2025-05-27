package de.westnordost.streetcomplete.data.changelog

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.html.HtmlElementNode
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlTextNode
import de.westnordost.streetcomplete.util.html.parseHtml
import de.westnordost.streetcomplete.util.ktx.getRawTextFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Changelog(private val resources: Resources) {
    /** Return the app's changelog, sorted descending by version.
     *
     *  @param sinceVersion optionally only return the changes since the given version
     */
    suspend fun getChangelog(sinceVersion: String? = null): Map<String, List<HtmlNode>> {
        val text = withContext(Dispatchers.IO) { resources.getRawTextFile(R.raw.changelog) }
        val html = withContext(Dispatchers.Default) { parseHtml(text) }
        var currentVersion: String? = null
        var versionHtml = ArrayList<HtmlNode>()
        // LinkedHashMap so that order is preserved
        val result = LinkedHashMap<String, List<HtmlNode>>()
        for (node in html) {
            if (node is HtmlElementNode && node.tag == "h2") {
                if (currentVersion != null) {
                    result[currentVersion] = versionHtml
                    versionHtml = ArrayList()
                }
                currentVersion = (node.nodes.first() as HtmlTextNode).text.trim()
                if (currentVersion == sinceVersion) return result
            } else {
                versionHtml.add(node)
            }
        }
        return result
    }
}
