package de.westnordost.streetcomplete.util.html

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlEntitiesKtTest {
    @Test fun unescapeHtmlEntities() {
        assertEquals(">", "&gt;".unescapeHtmlEntities())
        assertEquals("<", "&lt;".unescapeHtmlEntities())
        assertEquals("&", "&amp;".unescapeHtmlEntities())
        assertEquals("\"", "&quot;".unescapeHtmlEntities())
        assertEquals("&dunno;", "&dunno;".unescapeHtmlEntities())
    }

    @Test fun escapeHtmlEntities() {
        assertEquals("&gt;", ">".escapeHtmlEntities())
        assertEquals("&lt;", "<".escapeHtmlEntities())
        assertEquals("&amp;", "&".escapeHtmlEntities())
        assertEquals("&quot;", "\"".escapeHtmlEntities())
        assertEquals("#", "#".escapeHtmlEntities())
    }
}
