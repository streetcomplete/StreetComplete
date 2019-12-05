package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.toOpeningMonthsList
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import org.junit.Assert.*
import org.junit.Test


class OpeningHoursTagParserTest {
    @Test
    fun `reject gibberish, invalid or unwanted syntax`() {
        assertNull(OpeningHoursTagParser.parse("blatant gibberish"))
        assertNull(OpeningHoursTagParser.parse("Tuesday-Sunday 10:00-17:30")) // day names specified as full words
        assertNull(OpeningHoursTagParser.parse(""))
        assertNull(OpeningHoursTagParser.parse("Th 17:30-19:30; Th 17:00-19:00")) //rules overriding earlier rules
        assertNull(OpeningHoursTagParser.parse("9:00-20:00")) //without explicit days of week
        assertNull(OpeningHoursTagParser.parse("Su 09:00-48:00")) // too far into future, 26:00 would be accepted
        assertNull(OpeningHoursTagParser.parse("Mo 10:00-16:00/90")) // intervals
        assertNull(OpeningHoursTagParser.parse("8095652114")) // issue #26 in OpeningHoursParser is not crashing parser
        assertNull(OpeningHoursTagParser.parse("Mo-Th 17:30-19:30; Mo-Th 17:00-19:00")) // overriding earlier rule
        assertNull(OpeningHoursTagParser.parse("Mo-Th 17:30-19:30; Tu-Su 17:00-19:00")) // overriding earlier rule
        assertNull(OpeningHoursTagParser.parse("Mo-Th 17:30-19:30; Tu 17:00-19:00")) // overriding earlier rule
        assertNull(OpeningHoursTagParser.parse("Oct Mo 08:30-08:31;Oct Mo 08:30-10:30")) // overriding earlier rule
        assertNull(OpeningHoursTagParser.parse("Jan-Feb Mo 08:30-08:31;Oct-Jan Mo 08:30-10:30")) // overriding earlier rule
    }

    @Test
    fun `reject rules that are currently not supported but can be reasonably added`() {
        assertNull(OpeningHoursTagParser.parse("24/7"))

        // month range going over new year is not trivially added
        assertNull(OpeningHoursTagParser.parse("Oct-Jan Mo 08:30-10:30"))
        assertNull(OpeningHoursTagParser.parse("Oct-Jan Mo 08:30-10:30; Feb Mo 08:30-11:30"))
    }

    @Test
    fun `reject unimplemented for now syntax, supported by StreetComplete UI`() {
        assertNull(OpeningHoursTagParser.parse("Mo 09:00-20:00+")) //till last client

        // multiple days, not specified as range
        // note cases like "Mo,Tu,We 9:00-10:00" that will be either reject or transformed into "Mo-We 9:00-10:00"
        assertNull(OpeningHoursTagParser.parse("Mo,Tu 09:00-20:00"))
        assertNull(OpeningHoursTagParser.parse("Mo,Tu,Sa 09:00-20:00"))
    }

    @Test
    fun `reject valid syntax, too complex to be supported by SC or without opening hours`() {
        assertNull(OpeningHoursTagParser.parse("Mo sunrise-sunset")) // event based
        assertNull(OpeningHoursTagParser.parse("Mo 9:00-sunset")) // event based
        assertNull(OpeningHoursTagParser.parse("Mo-Sa 07:00-20:00; PH off")) //PH off part
        assertNull(OpeningHoursTagParser.parse("Th 17:30-19:30; Mo off")) // off modifier
        assertNull(OpeningHoursTagParser.parse("Mo-Sa")) // without hour range
        assertNull(OpeningHoursTagParser.parse("Jan-Dec")) // month range, without hours range
        assertNull(OpeningHoursTagParser.parse("Th 17:30-19:30; Jul-Sep Mo 17:00-19:00")) // partially month based
        assertNull(OpeningHoursTagParser.parse("Jul 01-Sep 19 Th 17:30-19:30")) // date range not limited to months
        assertNull(OpeningHoursTagParser.parse("Jul 01-Sep 30 Th 17:30-19:30")) // date range with explicit days
        assertNull(OpeningHoursTagParser.parse("Mo[1] 09:00-18:30")) // first monday within month
        assertNull(OpeningHoursTagParser.parse("Mo 06:00+")) // open ended, without end hour at all
        assertNull(OpeningHoursTagParser.parse("week 01-51 Mo 06:00-11:30")) // week indexing
        assertNull(OpeningHoursTagParser.parse("2000-2044 Mo 09:00-18:30")) // year ranges
        assertNull(OpeningHoursTagParser.parse("Mo-Fr 09:00-18:30 \"comment text\"")) // comments
        assertNull(OpeningHoursTagParser.parse("Jan+ Mo-Fr 09:00-18:30")) //open ended month ranges
        assertNull(OpeningHoursTagParser.parse("easter 09:00-18:00")) // easter
        // reject multiple lists of days in a single rule, as it would change form on processing
        // possibly with unexpected and unwanted results
        assertNull(OpeningHoursTagParser.parse("Mo-Fr 7:30-18:00, Sa-Su 9:00-18:00"))
    }

    @Test
    fun `accept valid opening hours`() {
        assertNotNull(OpeningHoursTagParser.parse("Mo-Su 00:00-24:00"))
        assertNotNull(OpeningHoursTagParser.parse("Mo 09:00-20:00"))
        assertNotNull(OpeningHoursTagParser.parse("Tu-Su 10:00-17:30"))
        assertNotNull(OpeningHoursTagParser.parse("We-Sa 09:30-15:00; Tu 12:00-18:30"))
        assertNotNull(OpeningHoursTagParser.parse("Mo 9-20")) //without explicitly specified minutes
        assertNotNull(OpeningHoursTagParser.parse("Mo-Sa 07:00-20:00; PH 7:00-7:05"))
        assertNotNull(OpeningHoursTagParser.parse("Jun Th 17:30-19:30"))
        assertNotNull(OpeningHoursTagParser.parse("Jul-Sep Mo 17:00-19:00"))
        assertNotNull(OpeningHoursTagParser.parse("Jun Th 17:30-19:30; Jul-Sep Mo 17:00-19:00"))
        assertNotNull(OpeningHoursTagParser.parse("Su 09:00-02:00")) // over midnight rules
        assertNotNull(OpeningHoursTagParser.parse("Su 09:00-26:00")) // over midnight, +24 syntax
        assertNotNull(OpeningHoursTagParser.parse("Mar-Oct Tu-Su 10:30-18:00; Nov-Dec Tu-Su 11:00-17:00"))
        assertNotNull(OpeningHoursTagParser.parse("Mar-Oct Tu-Su 10:30-18:00; Mar-Oct Mo 10:30-14:00; Nov-Dec Tu-Su 11:00-17:00; Nov-Dec Mo 11:00-14:00"))
        assertNotNull(OpeningHoursTagParser.parse("Th 17:30-19:30 open"))
        assertNotNull(OpeningHoursTagParser.parse("Mo-Su 09:00-12:00, 13:00-14:00")) // allow time gap
        assertNotNull(OpeningHoursTagParser.parse("Su-Mo 09:00-12:00")) // try to trigger bugs with range going end of week
        assertNotNull(OpeningHoursTagParser.parse("Fr-Mo 09:00-12:00")) // try to trigger bugs with range going end of week
        assertNotNull(OpeningHoursTagParser.parse("Mo 17:30-19:30; Th 17:00-19:00")) // multiple rules, no overrides

        // over weekend rules, without triggering collision detector
        assertNotNull(OpeningHoursTagParser.parse("Su-Mo 09:00-12:00; Tu-Sa 10:10-10:11"))
        assertNotNull(OpeningHoursTagParser.parse("Fr-Mo 09:00-12:00; Tu-Th 10:10-10:11"))
    }

    fun parseAndSave(openingHoursTag: String):String {
        return OpeningHoursTagParser.internalIntoTag((OpeningHoursTagParser.parse(openingHoursTag)!!.toOpeningMonthsList()))
    }


    @Test
    fun `saving data to a tag without data loss`() {
        assertEquals(parseAndSave("Mo 09:00-20:00"), "Mo 09:00-20:00")
        assertEquals(parseAndSave("Mo 09:00-20:00; Tu 00:00-24:00"), "Mo 09:00-20:00; Tu 00:00-24:00")
    }

    @Test
    fun `accept cosmetic changes without changing structure`() {
        assertEquals(parseAndSave("Tu-Su   10:30    -   18:00"), "Tu-Su 10:30-18:00")
        assertEquals(parseAndSave("Mo 09:00-20:00; Tu 09:00-20:00"), "Mo 09:00-20:00; Tu 09:00-20:00")

        // no merging of rules
        assertEquals(parseAndSave("Mo 09:00-20:00; Su 10:00-11:00; Tu 09:00-20:00"), "Mo 09:00-20:00; Su 10:00-11:00; Tu 09:00-20:00")

        assertEquals(parseAndSave("Mo 9-20"), "Mo 09:00-20:00") // add explicit minutes
        assertEquals(parseAndSave("Mar-Oct Tu-Su 10:30-18:00"), "Mar-Oct: Tu-Su 10:30-18:00") // adding :
        assertEquals(parseAndSave("Mar-Oct: Tu-Su 10:30-18:00"), "Mar-Oct: Tu-Su 10:30-18:00") // keep :
        assertEquals(parseAndSave("Mo 09:00-26:00"), "Mo 09:00-02:00") //26 -> 02
        assertEquals(parseAndSave("Th 17:30-19:30 open"), "Th 17:30-19:30") // drop explicit open
    }

    @Test
    fun `month limit has some impact on parser output`() {
        assertNotEquals(OpeningHoursTagParser.parse("Jan Mo 09:00-20:00"), OpeningHoursTagParser.parse("Mo 09:00-20:00"))
    }
    @Test
    fun `next day rules specified as over 24 hours, and sub 24 hours are treated equally`() {
        assertEquals(OpeningHoursTagParser.parse("Su 09:00-26:00"), OpeningHoursTagParser.parse("Su 09:00-02:00"))
    }

    @Test
    fun `next day rules specified as over 24 hours should be formatted as below 24 hours`() {
        val returned = OpeningHoursTagParser.parse("Su 09:00-26:00")
        assertNotEquals(returned, null)
        assertEquals(returned!!.size, 1)
        assertEquals(returned[0].months, OpeningMonthsRow().months)
        assertEquals(returned[0].weekdaysList.size, 1)
        assertEquals(returned[0].weekdaysList[0].timeRange.start, 9 * 60)
        assertNotEquals(returned[0].weekdaysList[0].timeRange.end, 26 * 60)
        assertEquals(returned[0].weekdaysList[0].timeRange.end, 2 * 60)
    }

    @Test
    fun `test full structure of a simple opening hour`() {
        val returned = OpeningHoursTagParser.parse("Mo 09:00-20:00")
        assertNotEquals(returned, null)
        assertEquals(returned!!.size, 1)
        assertEquals(returned[0].months, OpeningMonthsRow().months)
        assertEquals(returned[0].weekdaysList.size, 1)
        assertEquals(returned[0].weekdaysList[0].timeRange.start, 9 * 60)
        assertEquals(returned[0].weekdaysList[0].timeRange.end, 20 * 60)
        assertEquals(returned[0].weekdaysList[0].timeRange.isOpenEnded, false)
        val weekData = BooleanArray(8) { false }
        weekData.set(0, true)
        assertEquals(returned[0].weekdaysList[0].weekdays, Weekdays(weekData))
    }

    @Test
    fun `test full structure of a multiday opening hour with a gap`() {
        val returned = OpeningHoursTagParser.parse("Mo-Su 09:00-12:00, 13:00-14:00")
        assertNotEquals(returned, null)
        assertEquals(returned!!.size, 1)
        assertEquals(returned[0].months, OpeningMonthsRow().months)
        assertEquals(returned[0].weekdaysList.size, 2)
        assertEquals(returned[0].weekdaysList[0].timeRange.start, 9 * 60)
        assertEquals(returned[0].weekdaysList[0].timeRange.end, 12 * 60)
        assertEquals(returned[0].weekdaysList[0].timeRange.isOpenEnded, false)
        assertEquals(returned[0].weekdaysList[1].timeRange.start, 13 * 60)
        assertEquals(returned[0].weekdaysList[1].timeRange.end, 14 * 60)
        assertEquals(returned[0].weekdaysList[1].timeRange.isOpenEnded, false)
        val weekData = BooleanArray(7) { true }
        assertEquals(returned[0].weekdaysList[0].weekdays, Weekdays(weekData))
    }
}
