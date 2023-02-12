package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.osm.toCheckDateString
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ElementFilterOverpassKtTest {

    private val date2000_11_11 = FixedDate(LocalDate(2000, 11, 11))

    @Test fun tagOlderThan() {
        val date = dateDaysAgo(100f).toCheckDateString()
        assertEquals(
            "(if: date(timestamp()) < date('$date') || " +
                "date(t['opening_hours:check_date']) < date('$date') || " +
                "date(t['check_date:opening_hours']) < date('$date') || " +
                "date(t['opening_hours:lastcheck']) < date('$date') || " +
                "date(t['lastcheck:opening_hours']) < date('$date') || " +
                "date(t['opening_hours:last_checked']) < date('$date') || " +
                "date(t['last_checked:opening_hours']) < date('$date'))",
            TagOlderThan("opening_hours", RelativeDate(-100f)).toOverpassString()
        )
    }

    @Test fun tagNewerThan() {
        val date = dateDaysAgo(100f).toCheckDateString()
        assertEquals(
            "(if: date(timestamp()) > date('$date') || " +
                "date(t['opening_hours:check_date']) > date('$date') || " +
                "date(t['check_date:opening_hours']) > date('$date') || " +
                "date(t['opening_hours:lastcheck']) > date('$date') || " +
                "date(t['lastcheck:opening_hours']) > date('$date') || " +
                "date(t['opening_hours:last_checked']) > date('$date') || " +
                "date(t['last_checked:opening_hours']) > date('$date'))",
            TagNewerThan("opening_hours", RelativeDate(-100f)).toOverpassString()
        )
    }

    @Test fun notHasTagValueLike() {
        assertEquals(
            "[highway !~ '^(residential|unclassified)$']",
            NotHasTagValueLike("highway", "residential|unclassified").toOverpassString()
        )
        assertEquals(
            "[highway !~ '^(.*)$']",
            NotHasTagValueLike("highway", ".*").toOverpassString()
        )
    }

    @Test fun notHasTag() {
        assertEquals(
            "[highway != residential]",
            NotHasTag("highway", "residential").toOverpassString()
        )
        assertEquals(
            "['high:way' != residential]",
            NotHasTag("high:way", "residential").toOverpassString()
        )
        assertEquals(
            "[highway != 'resi:dential']",
            NotHasTag("highway", "resi:dential").toOverpassString()
        )
    }

    @Test fun notHasKey() {
        assertEquals("[!name]", NotHasKey("name").toOverpassString())
        assertEquals("[!'name:old']", NotHasKey("name:old").toOverpassString())
    }

    @Test fun notHasKeyLike() {
        assertEquals(
            "[!~'^(na[ms]e)$' ~ '.*']",
            NotHasKeyLike("na[ms]e").toOverpassString()
        )
    }

    @Test fun hasTagValueLike() {
        assertEquals(
            "[highway ~ '^(residential|unclassified)$']",
            HasTagValueLike("highway", "residential|unclassified").toOverpassString()
        )
        assertEquals(
            "[highway ~ '^(.*)$']",
            HasTagValueLike("highway", ".*").toOverpassString()
        )
    }

    @Test fun hasTag() {
        assertEquals(
            "[highway = residential]",
            HasTag("highway", "residential").toOverpassString()
        )
        assertEquals(
            "['high:way' = residential]",
            HasTag("high:way", "residential").toOverpassString()
        )
        assertEquals(
            "[highway = 'resi:dential']",
            HasTag("highway", "resi:dential").toOverpassString()
        )
    }

    @Test fun hasTagLike() {
        assertEquals(
            "[~'^(.ame)$' ~ '^(y.s)$']",
            HasTagLike(".ame", "y.s").toOverpassString()
        )
    }

    @Test fun notHasTagLike() {
        assertEquals(
            "[~'^(.ame)$' !~ '^(y.s)$']",
            NotHasTagLike(".ame", "y.s").toOverpassString()
        )
    }

    @Test fun hasTagLessThan() {
        assertEquals(
            "[width](if: number(t['width']) < 3.5)",
            HasTagLessThan("width", 3.5f).toOverpassString()
        )
        assertEquals(
            "['wid th'](if: number(t['wid th']) < 3.5)",
            HasTagLessThan("wid th", 3.5f).toOverpassString()
        )
        assertEquals(
            "[width](if: number(t['width']) < 3)",
            HasTagLessThan("width", 3f).toOverpassString()
        )
    }

    @Test fun hasTagLessOrEqualThan() {
        assertEquals(
            "[width](if: number(t['width']) <= 3.5)",
            HasTagLessOrEqualThan("width", 3.5f).toOverpassString()
        )
        assertEquals(
            "['wid th'](if: number(t['wid th']) <= 3.5)",
            HasTagLessOrEqualThan("wid th", 3.5f).toOverpassString()
        )
        assertEquals(
            "[width](if: number(t['width']) <= 3)",
            HasTagLessOrEqualThan("width", 3f).toOverpassString()
        )
    }

    @Test fun hasTagGreaterThan() {
        assertEquals(
            "[width](if: number(t['width']) > 3.5)",
            HasTagGreaterThan("width", 3.5f).toOverpassString()
        )
        assertEquals(
            "['wid th'](if: number(t['wid th']) > 3.5)",
            HasTagGreaterThan("wid th", 3.5f).toOverpassString()
        )
        assertEquals(
            "[width](if: number(t['width']) > 3)",
            HasTagGreaterThan("width", 3f).toOverpassString()
        )
    }

    @Test fun hasTagGreaterOrEqualThan() {
        assertEquals(
            "[width](if: number(t['width']) >= 3.5)",
            HasTagGreaterOrEqualThan("width", 3.5f).toOverpassString()
        )
        assertEquals(
            "['wid th'](if: number(t['wid th']) >= 3.5)",
            HasTagGreaterOrEqualThan("wid th", 3.5f).toOverpassString()
        )
        assertEquals(
            "[width](if: number(t['width']) >= 3)",
            HasTagGreaterOrEqualThan("width", 3f).toOverpassString()
        )
    }

    @Test fun hasKey() {
        assertEquals(
            "[name]",
            HasKey("name").toOverpassString()
        )
        assertEquals(
            "['name:old']",
            HasKey("name:old").toOverpassString()
        )
    }

    @Test fun hasKeyLike() {
        assertEquals(
            "[~'^(na[ms]e)$' ~ '.*']",
            HasKeyLike("na[ms]e").toOverpassString()
        )
    }

    @Test fun hasDateTagLessThan() {
        assertEquals(
            "[check_date](if: date(t['check_date']) < date('2000-11-11'))",
            HasDateTagLessThan("check_date", date2000_11_11).toOverpassString()
        )
    }

    @Test fun hasDateTagLessOrEqualThan() {
        assertEquals(
            "[check_date](if: date(t['check_date']) <= date('2000-11-11'))",
            HasDateTagLessOrEqualThan("check_date", date2000_11_11).toOverpassString()
        )
    }

    @Test fun hasDateTagGreaterThan() {
        assertEquals(
            "[check_date](if: date(t['check_date']) > date('2000-11-11'))",
            HasDateTagGreaterThan("check_date", date2000_11_11).toOverpassString()
        )
    }

    @Test fun hasDateTagGreaterOrEqualThan() {
        assertEquals(
            "[check_date](if: date(t['check_date']) >= date('2000-11-11'))",
            HasDateTagGreaterOrEqualThan("check_date", date2000_11_11).toOverpassString()
        )
    }

    @Test fun elementOlderThan() {
        val date = dateDaysAgo(10f).toCheckDateString()
        assertEquals(
            "(if: date(timestamp()) < date('$date'))",
            ElementOlderThan(RelativeDate(-10f)).toOverpassString()
        )
    }

    @Test fun elementNewerThanTest() {
        val date = dateDaysAgo(10f).toCheckDateString()
        assertEquals(
            "(if: date(timestamp()) > date('$date'))",
            ElementNewerThan(RelativeDate(-10f)).toOverpassString()
        )
    }

    @Test fun combineFilters() {
        assertEquals(
            "[hell][o]",
            CombineFilters(HasKey("hell"), HasKey("o")).toOverpassString()
        )
    }
}
