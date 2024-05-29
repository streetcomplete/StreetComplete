package de.westnordost.streetcomplete.util.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class HtmlParserKtTest {
    @Test fun doctype() {
        assertEquals(listOf(HtmlText("abc")), parse("<!DOCTYPE html>abc"))
        assertEquals(listOf(HtmlText("abc")), parse("<!doctype  HTML >abc"))
        assertFails { parse("<!doctype blubber>abc") }
        assertFails { parse("<!doctype html abc") }
    }

    @Test fun `one text`() {
        assertEquals(listOf(HtmlText("abc")), parse("abc"))
        assertEquals(listOf(HtmlText("<abc>")), parse("&lt;abc&gt;"))
        assertEquals(listOf(), parse(""))
    }

    @Test fun `one comment`() {
        assertEquals(listOf(), parse("<!--test-->"))
        assertEquals(listOf(), parse("<!---->"))
        assertEquals(listOf(HtmlText("a")), parse("<!--test-->a"))
        assertFails { parse("<!--") }
        assertFails { parse("<!-->hey-->") }
        assertFails { parse("<!--->hey-->") }
        assertFails { parse("<!--h--ey-->") }
        assertFails { parse("<!--hey--->") }
        assertFails { parse("<!--he\by-->") }
    }

    @Test fun `one cdata`() {
        assertEquals(listOf(), parse("<![CDATA[]]>"))
        assertEquals(listOf(), parse("<![CDATA[<>=\"']]>"))
        assertEquals(listOf(HtmlText("a")), parse("<![CDATA[]]>a"))
        assertFails { parse("<![CDATA[") }
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
        assertEquals(listOf(HtmlElement("a", mapOf("%" to "b"))), parse("<a %=b></a>"))
        assertEquals(listOf(HtmlElement("a", mapOf("%" to "#"))), parse("<a %=#></a>"))
        assertFails { parse("<a b=`></a>") }
        assertFails { parse("<a b/c=a></a>") }
        assertFails { parse("<a b= ></a>") }
        assertFails { parse("<a b='c></a>") }
        assertFails { parse("<a b='c'd'></a>") }
        assertFails { parse("<a b=\"c\"d\"></a>") }
        assertFails { parse("<a b='\b'></a>") }
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

    @Test fun `several elements`() {
        assertEquals(
            listOf(HtmlText("hello "), HtmlElement("and"), HtmlText(" bye ")),
            parse("hello <and><![CDATA[ <a>]]></and><!-- good--> bye ")
        )
    }

    @Test fun `nested elements`() {
        assertEquals(
            listOf(HtmlElement("a", nodes = listOf(HtmlText("hi")))),
            parse("<a>hi</a>")
        )
        assertEquals(
            listOf(
                HtmlElement("a", nodes = listOf(
                    HtmlText("h"), HtmlElement("b", nodes = listOf(
                        HtmlText("i")
                    ))
                ))
            ),
            parse("<a>h<b>i</b></a>")
        )
        assertFails { parse("<a>h<b>i</a></b>") }
    }
}


private fun parse(string: String) = parseHtmlMarkup(string).nodes
