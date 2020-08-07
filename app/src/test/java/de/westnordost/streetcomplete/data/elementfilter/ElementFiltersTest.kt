package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.meta.dateDaysAgo
import de.westnordost.streetcomplete.data.meta.toLastCheckDateString
import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class HasKeyTest {

    @Test fun matches() {
        val key = HasKey("name")

        assertTrue(key.matches(mapOf("name" to "yes")))
        assertTrue(key.matches(mapOf("name" to "no")))
        assertFalse(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("[name]", HasKey("name").toOverpassQLString())
        assertEquals("['name:old']", HasKey("name:old").toOverpassQLString())
    }
}

class NotHasKeyTest {

    @Test fun matches() {
        val key = NotHasKey("name")

        assertFalse(key.matches(mapOf("name" to "yes")))
        assertFalse(key.matches(mapOf("name" to "no")))
        assertTrue(key.matches(mapOf("neme" to "no")))
        assertTrue(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("[!name]", NotHasKey("name").toOverpassQLString())
        assertEquals("[!'name:old']", NotHasKey("name:old").toOverpassQLString())
    }
}

class HasKeyLikeTest {

    @Test fun matches() {
        val key = HasKeyLike("n.[ms]e")

        assertTrue(key.matches(mapOf("name" to "adsf")))
        assertTrue(key.matches(mapOf("nase" to "fefff")))
        assertTrue(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf("a name yo" to "no")))
        assertTrue(key.matches(mapOf("n(se" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("[~'^(na[ms]e)$' ~ '.*']", HasKeyLike("na[ms]e").toOverpassQLString())
    }
}

class HasTagTest {

    @Test fun matches() {
        val eq = HasTag("highway", "residential")

        assertTrue(eq.matches(mapOf("highway" to "residential")))
        assertFalse(eq.matches(mapOf("highway" to "residental")))
        assertFalse(eq.matches(mapOf("hipway" to "residential")))
        assertFalse(eq.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        val eq = HasTag("highway", "residential")
        assertEquals("[highway = residential]", eq.toOverpassQLString())
    }
}

class NotHasTagTest {

    @Test fun matches() {
        val neq = NotHasTag("highway", "residential")

        assertFalse(neq.matches(mapOf("highway" to "residential")))
        assertTrue(neq.matches(mapOf("highway" to "residental")))
        assertTrue(neq.matches(mapOf("hipway" to "residential")))
        assertTrue(neq.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        val neq = NotHasTag("highway", "residential")
        assertEquals("[highway != residential]", neq.toOverpassQLString())
    }
}

class HasTagValueLikeTest {

    @Test fun `matches like dot`() {
        val like = HasTagValueLike("highway", ".esidential")

        assertTrue(like.matches(mapOf("highway" to "residential")))
        assertTrue(like.matches(mapOf("highway" to "wesidential")))
        assertFalse(like.matches(mapOf("highway" to "rresidential")))
        assertFalse(like.matches(mapOf()))
    }

    @Test fun `matches like or`() {
        val like = HasTagValueLike("highway", "residential|unclassified")

        assertTrue(like.matches(mapOf("highway" to "residential")))
        assertTrue(like.matches(mapOf("highway" to "unclassified")))
        assertFalse(like.matches(mapOf("highway" to "blub")))
        assertFalse(like.matches(mapOf()))
    }

    @Test fun `groups values properly`() {
        val like = HasTagValueLike("highway", "residential|unclassified")

        assertEquals("[highway ~ '^(residential|unclassified)$']",like.toOverpassQLString())
    }

    @Test fun `key value to string`() {
        val eq = HasTagValueLike("highway", ".*")
        assertEquals("[highway ~ '^(.*)$']", eq.toOverpassQLString())
    }

}

class NotHasTagValueLikeTest {

    @Test fun `matches not like dot`() {
        val notlike = NotHasTagValueLike("highway", ".*")

        assertFalse(notlike.matches(mapOf("highway" to "anything")))
        assertTrue(notlike.matches(mapOf()))
    }

    @Test fun `matches not like or`() {
        val notlike = NotHasTagValueLike("noname", "yes")

        assertFalse(notlike.matches(mapOf("noname" to "yes")))
        assertTrue(notlike.matches(mapOf("noname" to "no")))
        assertTrue(notlike.matches(mapOf()))
    }

    @Test fun `groups values properly`() {
        val like = NotHasTagValueLike("highway", "residential|unclassified")

        assertEquals("[highway !~ '^(residential|unclassified)$']",like.toOverpassQLString())
    }

    @Test fun `key not value to string`() {
        val neq = NotHasTagValueLike("highway", ".*")
        assertEquals("[highway !~ '^(.*)$']", neq.toOverpassQLString())
    }
}

class HasTagLikeTest {

    @Test fun `matches regex key and value`() {
        val eq = HasTagLike(".ame", "y.s")

        assertTrue(eq.matches(mapOf("name" to "yes")))
        assertTrue(eq.matches(mapOf("lame" to "yos")))
        assertFalse(eq.matches(mapOf("lame" to "no")))
        assertFalse(eq.matches(mapOf("good" to "yes")))
        assertFalse(eq.matches(mapOf("neme" to "no")))
        assertFalse(eq.matches(mapOf("names" to "yess")))
        assertFalse(eq.matches(mapOf()))
    }

    @Test fun `to string`() {
        val eq = HasTagLike(".ame", "y.s")
        assertEquals("[~'^(.ame)$' ~ '^(y.s)$']", eq.toOverpassQLString())
    }
}

class HasTagGreaterThanTest {
    @Test fun matches() {
        val c = HasTagGreaterThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertTrue(c.matches(mapOf("width" to "3.6")))
        assertFalse(c.matches(mapOf("width" to "3.5")))
        assertFalse(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun `to string`() {
        val eq = HasTagGreaterThan("width", 3.5f)
        assertEquals("[width](if: number(t['width']) > 3.5)", eq.toOverpassQLString())
    }
}

class HasTagGreaterOrEqualThanTest {
    @Test fun matches() {
        val c = HasTagGreaterOrEqualThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertTrue(c.matches(mapOf("width" to "3.6")))
        assertTrue(c.matches(mapOf("width" to "3.5")))
        assertFalse(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun `to string`() {
        val eq = HasTagGreaterOrEqualThan("width", 3.5f)
        assertEquals("[width](if: number(t['width']) >= 3.5)", eq.toOverpassQLString())
    }
}

class HasTagLessThanTest {
    @Test fun matches() {
        val c = HasTagLessThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertFalse(c.matches(mapOf("width" to "3.6")))
        assertFalse(c.matches(mapOf("width" to "3.5")))
        assertTrue(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun `to string`() {
        val eq = HasTagLessThan("width", 3.5f)
        assertEquals("[width](if: number(t['width']) < 3.5)", eq.toOverpassQLString())
    }
}

class HasTagLessOrEqualThanTest {
    @Test fun matches() {
        val c = HasTagLessOrEqualThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertFalse(c.matches(mapOf("width" to "3.6")))
        assertTrue(c.matches(mapOf("width" to "3.5")))
        assertTrue(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun `to string`() {
        val eq = HasTagLessOrEqualThan("width", 3.5f)
        assertEquals("[width](if: number(t['width']) <= 3.5)", eq.toOverpassQLString())
    }
}

class HasDateTagGreaterThanTest {
    private val date = DATE_FORMAT.parse("2000-11-11")!!

    @Test fun matches() {
        val c = HasDateTagGreaterThan("check_date", date)
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-12")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-11")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun `to string`() {
        val eq = HasDateTagGreaterThan("check_date", date)
        assertEquals("[check_date](if: date(t['check_date']) > date('2000-11-11'))", eq.toOverpassQLString())
    }
}

class HasDateTagGreaterOrEqualThanTest {
    private val date = DATE_FORMAT.parse("2000-11-11")!!

    @Test fun matches() {
        val c = HasDateTagGreaterOrEqualThan("check_date", date)
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-12")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-11")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun `to string`() {
        val eq = HasDateTagGreaterOrEqualThan("check_date", date)
        assertEquals("[check_date](if: date(t['check_date']) >= date('2000-11-11'))", eq.toOverpassQLString())
    }
}

class HasDateTagLessThanTest {
    private val date = DATE_FORMAT.parse("2000-11-11")!!

    @Test fun matches() {
        val c = HasDateTagLessThan("check_date", date)
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-12")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-11")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun `to string`() {
        val eq = HasDateTagLessThan("check_date", date)
        assertEquals("[check_date](if: date(t['check_date']) < date('2000-11-11'))", eq.toOverpassQLString())
    }
}

class HasDateTagLessOrEqualThanTest {
    private val date = DATE_FORMAT.parse("2000-11-11")!!

    @Test fun matches() {
        val c = HasDateTagLessOrEqualThan("check_date", date)
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-12")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-11")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun `to string`() {
        val eq = HasDateTagLessOrEqualThan("check_date", date)
        assertEquals("[check_date](if: date(t['check_date']) <= date('2000-11-11'))", eq.toOverpassQLString())
    }
}

class TagOlderThanTest {
    private val oldDate = dateDaysAgo(101f)
    private val newDate = dateDaysAgo(99f)
    val c = TagOlderThan("opening_hours", 100f)

    @Test fun `does not match old element without tag`() {
        assertFalse(c.matches(mapOf("other" to "tag"), oldDate))
    }

    @Test fun `matches old element with tag`() {
        assertTrue(c.matches(mapOf("opening_hours" to "tag"), oldDate))
    }

    @Test fun `does not match new element with tag`() {
        assertFalse(c.matches(mapOf("opening_hours" to "tag"), newDate))
    }

    @Test fun `matches new element with tag and old check_date`() {
        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "opening_hours:check_date" to oldDate.toLastCheckDateString()
        ), newDate))

        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "check_date:opening_hours" to oldDate.toLastCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and old lastcheck`() {
        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "opening_hours:lastcheck" to oldDate.toLastCheckDateString()
        ), newDate))

        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "lastcheck:opening_hours" to oldDate.toLastCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and old last_checked`() {
        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to oldDate.toLastCheckDateString()
        ), newDate))

        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "last_checked:opening_hours" to oldDate.toLastCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and different check date tags of which only one is old`() {
        assertTrue(c.matches( mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to newDate.toLastCheckDateString(),
            "opening_hours:lastcheck" to newDate.toLastCheckDateString(),
            "opening_hours:check_date" to newDate.toLastCheckDateString(),
            "last_checked:opening_hours" to oldDate.toLastCheckDateString(),
            "lastcheck:opening_hours" to newDate.toLastCheckDateString(),
            "check_date:opening_hours" to newDate.toLastCheckDateString()
        ), newDate))
    }
}

private fun ElementFilter.matches(tags: Map<String,String>, date: Date? = null): Boolean =
    matches(OsmNode(1, 1, 0.0, 0.0, tags, null, date))

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)