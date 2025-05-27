package de.westnordost.streetcomplete.util.html

sealed interface HtmlNode

data class HtmlElementNode(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val nodes: List<HtmlNode> = emptyList()
) : HtmlNode

data class HtmlTextNode(val text: String) : HtmlNode
