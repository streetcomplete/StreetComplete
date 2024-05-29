package de.westnordost.streetcomplete.util.html

sealed interface HtmlNode

data class HtmlRoot(val nodes: List<HtmlNode>) : HtmlNode

data class HtmlElement(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val nodes: List<HtmlNode> = emptyList()
): HtmlNode

data class HtmlText(val text: String) : HtmlNode
