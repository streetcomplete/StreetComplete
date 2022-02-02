package de.westnordost.streetcomplete.quests.road_name

import org.junit.Assert.assertEquals
import org.junit.Test

class RoadNameSuggestionsSourceTest {

    @Test fun toRoadNameByLanguage() {
        val result = mapOf(
            "name" to "näm",
            "int_name" to "ünt_näm",
            "alt_name" to "ält_näm",
            "name:de" to "zörmän näm",
            "name:th-Latn" to "ziämäs näm in Lätin",
            "name:source" to "sörce näm",
            "name:etymology:wikidata" to "Q1234",
            "name:guj" to "güjäräti näm"
        ).toRoadNameByLanguage()

        assertEquals(
            mapOf(
                "" to "näm",
                "international" to "ünt_näm",
                "de" to "zörmän näm",
                "th-Latn" to "ziämäs näm in Lätin",
                "guj" to "güjäräti näm"
            ),
            result
        )
    }
}
