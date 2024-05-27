package de.westnordost.streetcomplete.util.html

sealed interface HtmlNode

data class HtmlElement(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: List<HtmlNode> = emptyList()
): HtmlNode

data class HtmlText(val text: String) : HtmlNode

data class HtmlComment(val comment: String) : HtmlNode
