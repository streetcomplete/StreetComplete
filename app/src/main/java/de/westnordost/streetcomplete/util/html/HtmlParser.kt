package de.westnordost.streetcomplete.util.html

import de.westnordost.streetcomplete.util.StringWithCursor

/** Parses some HTML for markup
 *
 *  The parser has the following limitations:
 *  - only the character references (=HTML entities) `&amp;` `&quot;` `&lt;` and `&gt;` are recognized
 *
 *  @throws HtmlParseException
 */
fun parseHtml(string: String): List<HtmlNode> {
    val cursor = StringWithCursor(string.replace(ignoredElementsRegex, ""))
    val result = cursor.parseNodes()
    if (!cursor.isAtEnd()) cursor.fail("Unexpected end of string")
    return result
}

/**
 * Same as `parseHtml` but on a parsing error, will return a single text node with the string.
 */
fun tryParseHtml(string: String): List<HtmlNode> = try {
    parseHtml(string)
} catch (e: HtmlParseException) {
    listOf(HtmlTextNode(string))
}

private fun StringWithCursor.parseNodes(): List<HtmlNode> {
    val nodes = ArrayList<HtmlNode>()
    while (!isAtEnd()) {
        val element = parseElement()
        if (element != null) {
            nodes.add(element)
            continue
        }
        val text = parseText()
        if (text != null) {
            nodes.add(HtmlTextNode(text))
            continue
        }
        break
    }
    return nodes
}

private fun StringWithCursor.parseElement(): HtmlElementNode? {
    val start = cursor
    if (!nextIsAndAdvance('<')) return null
    // start tag with attributes
    val tag = getNextWordAndAdvance { it.isAlphanumeric() }?.lowercase()
    if (tag == null) {
        cursor = start
        return null
    }
    skipWhitespaces()
    val attributes = parseAttributes()
    skipWhitespaces()
    nextIsAndAdvance('/') // ignore closing tag
    if (!nextIsAndAdvance('>')) fail("Expected >")

    if (tag in voidTags) return HtmlElementNode(tag, attributes)

    val children = parseNodes()

    // end tag
    if (!isAtEnd()) {
        if (!nextIsAndAdvance("</$tag", ignoreCase = true)) fail("Expected end tag")
        skipWhitespaces()
        if (!nextIsAndAdvance('>')) fail("Expected >")
    }
    return HtmlElementNode(tag, attributes, children)
}

private fun StringWithCursor.parseText(): String? {
    // convert all whitespaces (including tab, linefeed, ...) to spaces and then ensure that there
    // are no spaces next to each other
    val chars = ArrayList<Char>()
    while (!isAtEnd() && !nextIs('<')) {
        var c = advance()
        if (c.isWhitespace()) c = ' '
        if (c != ' ' || chars.lastOrNull() != ' ') chars.add(c)
    }
    if (chars.isEmpty()) return null
    return String(chars.toCharArray()).replaceHtmlEntities()
}

private fun StringWithCursor.parseAttributes(): Map<String, String> {
    val attributes = ArrayList<Pair<String, String>>()
    while (!isAtEnd()) {
        val pair = parseAttribute() ?: break
        attributes.add(pair)
        skipWhitespaces()
    }
    return attributes.toMap()
}

private fun StringWithCursor.parseAttribute(): Pair<String, String>? {
    val name = getNextWordAndAdvance { it !in notAllowedCharactersInAttributeName } ?: return null
    skipWhitespaces()
    if (!nextIsAndAdvance('=')) return name to ""
    skipWhitespaces()
    val value: String?
    if (nextIsAndAdvance('\'')) {
        val end = findNext('\'')
        if (isAtEnd(end)) fail("Expected closing ' for attribute value")
        value = advanceBy(end)
        advance()
    } else if (nextIsAndAdvance('"')) {
        val end = findNext('"')
        if (isAtEnd(end)) fail("Expected closing \" for attribute value")
        value = advanceBy(end)
        advance()
    } else {
        value = getNextWordAndAdvance { it !in notAllowedCharactersInUnquotedAttributeValue }
        if (value == null) fail("Expected alphanumeric attribute value")
    }
    if (value.any { it.isISOControl() }) fail("Attribute value contains control characters")
    return name to value.replaceHtmlEntities()
}

private fun StringWithCursor.skipWhitespaces(): Int =
    advanceWhile { it.isWhitespace() }

private fun StringWithCursor.fail(message: String): Nothing =
    throw HtmlParseException(cursor, message)

private fun Char.isAlphanumeric(): Boolean =
    this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'

private fun String.replaceHtmlEntities(): String =
    replace(entityRegex) { entities[it.value]?.toString() ?: it.value }

// https://developer.mozilla.org/en-US/docs/Glossary/Void_element
private val voidTags = setOf(
    "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr"
)

private val notAllowedCharactersInUnquotedAttributeValue =
    setOf(' ', '"', '\'', '=', '<', '>', '`')

private val notAllowedCharactersInAttributeName =
    setOf(' ', '"', '\'', '>', '/', '=')

// cdata sections, comments, doctype at start
private val ignoredElementsRegex by lazy {
    Regex(
        "^<!DOCTYPE +?HTML *?>|<!\\[CDATA\\[.*?]]>|<!--.*?-->",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
}
private val entityRegex by lazy { Regex("&[a-zA-Z0-9]+;") }

private val entities = mapOf(
    "&quot;" to '"',
    "&amp;" to '&',
    "&lt;" to '<',
    "&gt;" to '>',
)
