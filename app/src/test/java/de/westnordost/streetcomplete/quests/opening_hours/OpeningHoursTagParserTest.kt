package de.westnordost.streetcomplete.quests.opening_hours

import org.junit.Assert
import org.junit.Test


class OpeningHoursTagParserTest {
    @Test
    fun `reject gibberish`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("blatant gibberish"), null)
    }

    @Test
    fun `accept simple valid opening hours`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Mo 09:00-20:00"), null)
    }

    @Test
    fun `month limit has some impact on parser output`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Jan Mo 09:00-20:00"), OpeningHoursTagParser.parse("Mo 09:00-20:00"))
    }

    @Test
    fun `accept opening hours covering multiple days`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Tu-Su 10:00-17:30"), null)
    }

    @Test
    fun `reject opening hours with incorrect full word day names`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Tuesday-Sunday 10:00-17:30"), null)
    }

    @Test
    fun `accept opening hours covering multiple days with different open times`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("We-Sa 09:30-15:00; Tu 12:00-18:30"), null)
    }

    @Test
    fun `reject opening hours without explicit days of week`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("9:00-20:00"), null)
    }

    @Test
    fun `reject open all day, every day specified in shortcut form as not representable`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("24/7"), null)
    }

    @Test
    fun `accept open all day, every day specified in day based form as representable`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Mo-Su 00:00-24:00"), null)
    }

    @Test
    fun `reject empty rule`() {
        Assert.assertEquals(OpeningHoursTagParser.parse(""), null)
    }

    @Test
    fun `reject rules overriding earlier rules`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Th 17:30-19:30; Th 17:00-19:00"), null)
    }

    @Test
    fun `reject rules overriding earlier rules in the month mode`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Oct Mon 08:30-08:31;Oct Mon 08:30-10:30"), null)
    }

    @Test
    fun `accept rules not overriding earlier rules`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Mo 17:30-19:30; Th 17:00-19:00"), null)
    }

    @Test
    fun `reject implied full day rules`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo-Sa"), null)
    }

    @Test
    fun `reject implied full day rules by month only specs`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Jan-Dec"), null)
    }

    @Test
    fun `reject event based rules`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo sunrise-sunset"), null)
    }

    @Test
    fun `reject even partially event based rules`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo 9:00-sunset"), null)
    }

    @Test
    fun `reject rules with PH off part as not representable`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo-Sa 07:00-20:00; PH off"), null)
    }

    @Test
    fun `allow rules with PH open time part as representable`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Mo-Sa 07:00-20:00; PH 7:00-7:05"), null)
    }

    @Test
    fun `reject where only some rules are month based`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Th 17:30-19:30; Jul-Sep Mo 17:00-19:00"), null)
    }

    @Test
    fun `allow month based rules with single month`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Jun Th 17:30-19:30"), null)
    }

    @Test
    fun `allow month based rules with month range`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Jul-Sep Mo 17:00-19:00"), null)
    }

    @Test
    fun `allow month based rules where all rules are month based and covering entire months with multiple month ranges`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Jun Th 17:30-19:30; Jul-Sep Mo 17:00-19:00"), null)
    }

    @Test
    fun `reject rules with off part as not representable`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Th 17:30-19:30; Mo off"), null)
    }

    @Test
    fun `reject rules for date based rules not based on full months`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Jul 01-Sep 19 Th 17:30-19:30"), null)
    }

    @Test
    fun `reject rules for date based rules using more precise dating than full months, even if with an equal result`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Jul 01-Sep 30 Th 17:30-19:30"), null)
    }

    @Test
    fun `reject overflow rules specified with over 24 hours`() {
        // TODO, maybe this should be accepted after all? Note conflict with the next rule
        Assert.assertEquals(OpeningHoursTagParser.parse("Su 09:00-26:00"), null)
    }

    @Test
    fun `allow next day rules, for objects open also during early part of the next day, in this case Monday`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Su 09:00-02:00"), null)
    }

    @Test
    fun `complex month based rules are accepted`() {
        val oh = "Mar-Oct Tu-Su 10:30-18:00; Nov-Dec Tu-Su 11:00-17:00"
        Assert.assertNotEquals(OpeningHoursTagParser.parse(oh), null)
    }

    @Test
    fun `very complex month based rules are accepted`() {
        val oh = "Mar-Oct Tu-Su 10:30-18:00; Mar-Oct Mo 10:30-14:00; Nov-Dec Tu-Su 11:00-17:00; Nov-Dec Mo 11:00-14:00"
        Assert.assertNotEquals(OpeningHoursTagParser.parse(oh), null)
    }

    @Test
    fun `rules with explicit open are rejected`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Th 17:30-19:30 open"), null)
    }

    @Test
    fun `allow rules with a gap of time`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Mo-Su 09:00-12:00, 13:00-14:00"), null)
    }

    @Test
    fun `allow end day on earlier day of week than the start day`() {
        Assert.assertNotEquals(OpeningHoursTagParser.parse("Su-Mo 09:00-12:00"), null)
    }

    @Test
    fun `reject multiple lists of days in a single rule, as it would change form on processing, possibly with unexpected and unwanted results`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo-Fr 7:30-18:00, Sa-Su 9:00-18:00"), null)
    }

    @Test
    fun `reject indexing day of week within month ("first monday") as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo[1] 09:00-18:30"), null)
    }

    @Test
    fun `reject open ended as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo 06:00+"), null)
    }

    @Test
    fun `reject week indexing as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("week 01-51 Mo 06:00-11:30"), null)
    }

    @Test
    fun `reject year ranges as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("2000-2044 Mo,Tu,Th,Fr 09:00-18:30"), null)
    }

    @Test
    fun `reject comments as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Mo-Fr 09:00-18:30 \"comment text\""), null)
    }

    @Test
    fun `reject open ended months as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("Jan+ Mo-Fr 09:00-18:30"), null)
    }

    @Test
    fun `reject easter in opening hours as not supported by SC`() {
        Assert.assertEquals(OpeningHoursTagParser.parse("easter 09:00-18:00"), null)
    }
}
