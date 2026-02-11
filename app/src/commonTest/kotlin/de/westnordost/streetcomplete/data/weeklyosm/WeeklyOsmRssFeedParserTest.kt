package de.westnordost.streetcomplete.data.weeklyosm

import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WeeklyOsmRssFeedParserTest {
    @Test fun parse() {
        // snippet from actual RSS feed
        val buffer = Buffer()
        buffer.writeString("""
            <?xml version="1.0" encoding="UTF-8"?><rss version="2.0"
                xmlns:content="http://purl.org/rss/1.0/modules/content/"
                xmlns:wfw="http://wellformedweb.org/CommentAPI/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
                xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
                >

            <channel>
                <title>weekly &#8211; semanario &#8211; hebdo &#8211; 週刊 &#8211; týdeník &#8211; Wochennotiz &#8211; 주간 &#8211; tygodnik</title>
                <atom:link href="https://weeklyosm.eu/feed" rel="self" type="application/rss+xml" />
                <link>https://weeklyosm.eu</link>
                <description>A summary of things happening in the OpenStreetMap world</description>
                <lastBuildDate>Sun, 01 Feb 2026 13:34:06 +0000</lastBuildDate>
                <language>en-GB</language>
                <sy:updatePeriod>
                hourly	</sy:updatePeriod>
                <sy:updateFrequency>
                1	</sy:updateFrequency>


            <image>
                <url>https://weeklyosm.eu/wp-content/uploads/2022/03/favicon_192px-150x150.png</url>
                <title>weekly &#8211; semanario &#8211; hebdo &#8211; 週刊 &#8211; týdeník &#8211; Wochennotiz &#8211; 주간 &#8211; tygodnik</title>
                <link>https://weeklyosm.eu</link>
                <width>32</width>
                <height>32</height>
            </image>
            <site xmlns="com-wordpress:feed-additions:1">192242954</site>	<item>
                    <title>weeklyOSM 810</title>
                    <link>https://weeklyosm.eu/archives/18388</link>
                                <comments>https://weeklyosm.eu/archives/18388#respond</comments>

                    <dc:creator><![CDATA[weeklyteam]]></dc:creator>
                    <pubDate>Sun, 01 Feb 2026 12:14:16 +0000</pubDate>
            """.trimIndent()
        )

        val instant = WeeklyOsmRssFeedParser().parseLastPublishDate(buffer)
        assertNotNull(instant)
        val dateTime = instant.toLocalDateTime(TimeZone.UTC)
        assertEquals(2026, dateTime.year)
        assertEquals(Month.FEBRUARY, dateTime.month)
        assertEquals(1, dateTime.dayOfMonth)
        assertEquals(12, dateTime.hour)
        assertEquals(14, dateTime.minute)
        assertEquals(16, dateTime.second)
    }
}
