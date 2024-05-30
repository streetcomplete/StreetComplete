package de.westnordost.streetcomplete.util.html

import de.westnordost.streetcomplete.util.StringWithCursor

/** Parses some HTML for markup
 *
 *  The parser has the following limitations:
 *  - only the character references (=HTML entities) `&amp;` `&quot;` `&lt;` and `&gt;` are recognized
 *
 *  @throws HtmlParseException
 *  */
fun parseHtml(string: String): List<HtmlNode> =
    StringWithCursor(string).parseRoot()

private fun StringWithCursor.parseRoot(): List<HtmlNode> {
    // ignore starting <!doctype html>
    if (nextIsAndAdvance("<!doctype", ignoreCase = true)) {
        skipWhitespaces()
        if (!nextIsAndAdvance("html", ignoreCase = true)) fail("Invalid doctype")
        skipWhitespaces()
        if (!nextIsAndAdvance('>')) fail("Missing >")
    }
    val result = parseNodes()
    if (!isAtEnd()) fail("Unexpected end of string")
    return result
}

private fun StringWithCursor.parseNodes(): List<HtmlNode> {
    val children = ArrayList<HtmlNode>()
    while (!isAtEnd()) {
        val element = parseElement()
        if (element != null) {
            children.add(element)
            continue
        }
        if (parseComment() != null) {
            continue
        }
        if (parseCdataSection() != null) {
            continue
        }
        val text = parseText()
        if (text != null) {
            children.add(HtmlTextNode(text))
            continue
        }
        break
    }
    return children
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
        if (!nextIsAndAdvance('<')) fail("Expected end tag")
        if (!nextIsAndAdvance('/')) fail("Expected /")
        if (!nextIsAndAdvance(tag, ignoreCase = true)) fail("Expected end tag")
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

private fun StringWithCursor.parseComment(): String? {
    if (!nextIsAndAdvance("<!--")) return null
    val comment = advanceBy(findNext("-->"))
    if (comment.startsWith('>') || comment.startsWith("->") ||
        comment.endsWith('-') || comment.indexOf("--") != -1) {
        fail("Malformed comment")
    }
    if (!nextIsAndAdvance("-->")) fail("Expected end of comment")
    return comment
}

private fun StringWithCursor.parseCdataSection(): String? {
    if (!nextIsAndAdvance("<![CDATA[", ignoreCase = true)) return null
    val cdata = advanceBy(findNext("]]>"))
    if (!nextIsAndAdvance("]]>")) fail("Expected end of cdata")
    return cdata
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
    this in 'a'..'z' || this in 'A' .. 'Z' || this in '0' .. '9'

private fun String.replaceHtmlEntities(): String =
    replace(entityRegex) { entities[it.value]?.toString() ?: it.value }

// https://developer.mozilla.org/en-US/docs/Glossary/Void_element
private val voidTags = setOf(
    "area","base","br","col","embed","hr","img","input","link","meta","param","source","track","wbr"
)

private val notAllowedCharactersInUnquotedAttributeValue =
    setOf(' ', '"', '\'', '=', '<', '>', '`')

private val notAllowedCharactersInAttributeName =
    setOf(' ', '"', '\'', '>', '/', '=')

private val entityRegex by lazy { Regex("&[a-zA-Z0-9]+;") }

private val entities = mapOf(
    "&quot;" to '"',
    "&amp;" to '&',
    "&lt;" to '<',
    "&gt;" to '>',
)
