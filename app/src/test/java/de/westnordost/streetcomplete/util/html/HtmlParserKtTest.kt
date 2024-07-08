package de.westnordost.streetcomplete.util.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class HtmlParserKtTest {
    @Test fun doctype() {
        assertEquals(listOf(HtmlTextNode("abc")), parse("<!DOCTYPE html>abc"))
        assertEquals(listOf(HtmlTextNode("abc")), parse("<!doctype  HTML >abc"))
        assertFails { parse("<!doctype blubber>abc") }
        assertFails { parse("<!doctype html abc") }
    }

    @Test fun `one text`() {
        assertEquals(listOf(HtmlTextNode("abc")), parse("abc"))
        assertEquals(listOf(HtmlTextNode(" a b c ")), parse("  a\n   b\tc    "))
        assertEquals(listOf(HtmlTextNode("<abc>")), parse("&lt;abc&gt;"))
        assertEquals(listOf(), parse(""))
    }

    @Test fun `one comment`() {
        assertEquals(listOf(), parse("<!--test-->"))
        assertEquals(listOf(), parse("<!---->"))
        assertEquals(listOf(HtmlTextNode("a")), parse("<!--test-->a"))
        assertFails { parse("<!--") }
    }

    @Test fun `one cdata`() {
        assertEquals(listOf(), parse("<![CDATA[]]>"))
        assertEquals(listOf(), parse("<![CDATA[<>=\"']]>"))
        assertEquals(listOf(HtmlTextNode("a")), parse("<![CDATA[]]>a"))
        assertFails { parse("<![CDATA[") }
    }

    @Test fun `one simple element`() {
        assertEquals(listOf(HtmlElementNode("a")), parse("<a></a>"))
        assertEquals(listOf(HtmlElementNode("a")), parse("<a ></a >"))
        assertEquals(listOf(HtmlElementNode("abc")), parse("<AbC></aBc>"))
        assertEquals(listOf(HtmlElementNode("a")), parse("<a>"))
        assertEquals(listOf(HtmlElementNode("a")), parse("<a/>"))
        assertFails { parse("<one:two></one:two>") }
        assertFails { parse("<#></#>") }
    }

    @Test fun `nested elements with same name`() {
        assertEquals(
            listOf(HtmlElementNode("p",
                nodes = listOf(HtmlElementNode("p",
                    nodes = listOf(HtmlTextNode("a"))
                ))
            )),
            parse("""<p><p>a</p></p>""")
        )
    }

    @Test fun `one void element`() {
        assertEquals(listOf(HtmlElementNode("br"), HtmlElementNode("br")), parse("<br><br>"))
        assertEquals(listOf(HtmlElementNode("br"), HtmlElementNode("br")), parse("<br/><br>"))
        assertFails { parse("<br></br>") }
    }

    @Test fun `element with attribute`() {
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to ""))), parse("<a b></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c"))), parse("<a b=c></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c"))), parse("<a b = c></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c"))), parse("<a b = c ></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c d"))), parse("<a b='c d'></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c d"))), parse("<a b=\"c d\"></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "c\"d"))), parse("<a b=\"c&quot;d\"></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("%" to "b"))), parse("<a %=b></a>"))
        assertEquals(listOf(HtmlElementNode("a", mapOf("%" to "#"))), parse("<a %=#></a>"))
        assertFails { parse("<a b=`></a>") }
        assertFails { parse("<a b/c=a></a>") }
        assertFails { parse("<a b= ></a>") }
        assertFails { parse("<a b='c></a>") }
        assertFails { parse("<a b='c'd'></a>") }
        assertFails { parse("<a b=\"c\"d\"></a>") }
        assertFails { parse("<a b='\b'></a>") }
    }

    @Test fun `element with attributes`() {
        assertEquals(listOf(HtmlElementNode("a", mapOf("b" to "", "c" to ""))), parse("<a b c></a>"))
        assertEquals(
            listOf(HtmlElementNode("a", mapOf("b" to "", "c" to "d"))),
            parse("<a b c=d></a>")
        )
        assertEquals(
            listOf(HtmlElementNode("a", mapOf("b" to "c", "d" to "e"))),
            parse("<a b='c'd='e'></a>")
        )
    }

    @Test fun `several elements`() {
        assertEquals(
            listOf(HtmlTextNode("hello "), HtmlElementNode("and"), HtmlTextNode(" bye ")),
            parse("hello <and><![CDATA[ <a>]]></and><!-- good--> bye ")
        )
    }

    @Test fun `nested elements`() {
        assertEquals(
            listOf(HtmlElementNode("a", nodes = listOf(HtmlTextNode("hi")))),
            parse("<a>hi</a>")
        )
        assertEquals(
            listOf(
                HtmlElementNode("a", nodes = listOf(
                    HtmlTextNode("h"), HtmlElementNode("b", nodes = listOf(
                        HtmlTextNode("i")
                    ))
                ))
            ),
            parse("<a>h<b>i</b></a>")
        )
        assertFails { parse("<a>h<b>i</a></b>") }
    }
}

private fun parse(string: String) = parseHtml(string)
