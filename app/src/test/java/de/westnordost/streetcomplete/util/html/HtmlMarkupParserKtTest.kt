package de.westnordost.streetcomplete.util.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class HtmlMarkupParserKtTest {
    @Test fun `one text`() {
        assertEquals(listOf(HtmlText("abc")), parse("abc"))
        assertEquals(listOf(HtmlText("<abc>")), parse("&lt;abc&gt;"))
        assertEquals(listOf(), parse(""))
        assertFails { parse("ab\bc") }
    }

    @Test fun `one comment`() {
        assertEquals(listOf(HtmlComment("test")), parse("<!--test-->"))
        assertEquals(listOf(HtmlComment(" test ")), parse("<!-- test -->"))
        assertEquals(listOf(HtmlComment("")), parse("<!---->"))
        assertEquals(listOf(HtmlComment("<>[]{}()-:%\"'#$|~")), parse("<!--<>[]{}()-:%\"'#$|~-->"))
        assertFails { parse("<!--ab\bc-->") }
        assertFails { parse("<!--") }
        assertFails { parse("<!-->hey-->") }
        assertFails { parse("<!--->hey-->") }
        assertFails { parse("<!--h--ey-->") }
        assertFails { parse("<!--hey--->") }
        assertFails { parse("<!--he\by-->") }
    }

    @Test fun `one simple element`() {
        assertEquals(listOf(HtmlElement("a")), parse("<a></a>"))
        assertEquals(listOf(HtmlElement("a")), parse("<a ></a >"))
        assertEquals(listOf(HtmlElement("abc")), parse("<AbC></aBc>"))
        assertEquals(listOf(HtmlElement("a")), parse("<a>"))
        assertEquals(listOf(HtmlElement("a")), parse("<a/>"))
        assertFails { parse("<one:two></one:two>") }
        assertFails { parse("<#></#>") }
    }

    @Test fun `one void element`() {
        assertEquals(listOf(HtmlElement("br"), HtmlElement("br")), parse("<br><br>"))
        assertEquals(listOf(HtmlElement("br"), HtmlElement("br")), parse("<br/><br>"))
        assertFails { parse("<br></br>") }
    }

    @Test fun `element with attribute`() {
        assertEquals(listOf(HtmlElement("a", mapOf("b" to ""))), parse("<a b></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c"))), parse("<a b=c></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c"))), parse("<a b = c></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c"))), parse("<a b = c ></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c d"))), parse("<a b='c d'></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c d"))), parse("<a b=\"c d\"></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "c\"d"))), parse("<a b=\"c&quot;d\"></a>"))
        assertFails { parse("<a b= ></a>") }
        assertFails { parse("<a %=b></a>") }
        assertFails { parse("<a b='c></a>") }
        assertFails { parse("<a b='c'd'></a>") }
        assertFails { parse("<a b=\"c\"d\"></a>") }
    }

    @Test fun `element with attributes`() {
        assertEquals(listOf(HtmlElement("a", mapOf("b" to "", "c" to ""))), parse("<a b c></a>"))
        assertEquals(
            listOf(HtmlElement("a", mapOf("b" to "", "c" to "d"))),
            parse("<a b c=d></a>")
        )
        assertEquals(
            listOf(HtmlElement("a", mapOf("b" to "c", "d" to "e"))),
            parse("<a b='c'd='e'></a>")
        )
    }

    // TODO several elements

    // TODO nested elements
}


private fun parse(string: String) = parseHtmlMarkup(string)
