package de.westnordost.streetcomplete.data.changelog

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.util.html.HtmlElementNode
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlTextNode
import de.westnordost.streetcomplete.util.html.parseHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Actually a LinkedHashMap (= ordered by insertion order).
 *  In this case, ordered by version descending
 */
typealias Changelog = Map<String, List<HtmlNode>>

/** Return the app's changelog - a map of version name to a list of
 *  [HtmlNode][HtmlNode]s, sorted descending by version.
 *
 *  @param sinceVersion optionally only return the changes since the given version
 */
suspend fun Res.readChangelog(sinceVersion: String? = null): Changelog {
    val text = readBytes("files/changelog.html").decodeToString()
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
