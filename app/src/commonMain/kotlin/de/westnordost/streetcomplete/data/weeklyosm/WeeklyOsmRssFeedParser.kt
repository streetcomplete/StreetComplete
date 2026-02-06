package de.westnordost.streetcomplete.data.weeklyosm

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.RFC_2822_STRICT
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.io.Source
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.allText
import nl.adaptivity.xmlutil.core.kxio.newReader
import nl.adaptivity.xmlutil.xmlStreaming

/** This cannot really be called an RSS parser. It actually only checks the time the last article
 *  has been published :-D */
class WeeklyOsmRssFeedParser() {
    /** Parse at what time the last edition of weekly OSM has been released */
    fun parseLastPublishDate(source: Source): Instant? {
        val reader = xmlStreaming.newReader(source)
        while (reader.hasNext()) {
            if (reader.next() == START_ELEMENT) {
                if (reader.localName == "pubDate") {
                    try {
                        return Instant.parse(
                            input = reader.allText(),
                            format = DateTimeComponents.Formats.RFC_2822_STRICT
                        )
                    } catch (e: IllegalArgumentException) {
                        return null
                    }
                }
            }
        }
        return null
    }
}
