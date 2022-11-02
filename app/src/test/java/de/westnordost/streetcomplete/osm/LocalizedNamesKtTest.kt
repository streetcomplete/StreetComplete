package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

internal class LocalizedNamesKtTest {

    @Test fun createLocalizedNames() {
        assertEquals(
            listOf(
                LocalizedName("", "näm"), // name at first position
                LocalizedName("international", "ünt_näm"),
                LocalizedName("de", "zörmän näm"),
                LocalizedName("th-Latn", "ziämäs näm in Lätin"),
                LocalizedName("guj", "güjäräti näm")
            ),
            createLocalizedNames(mapOf(
                "int_name" to "ünt_näm",
                "alt_name" to "ält_näm",
                "name:de" to "zörmän näm",
                "name" to "näm", // name not at first position
                "name:th-Latn" to "ziämäs näm in Lätin",
                "name:source" to "sörce näm",
                "name:etymology:wikidata" to "Q1234",
                "name:guj" to "güjäräti näm"
            ))
        )
    }

    @Test fun localizedNamesToTags() {
        val builder = StringMapChangesBuilder(mapOf())
        listOf(
            LocalizedName("", "näm"),
            LocalizedName("international", "ünt_näm"),
            LocalizedName("de", "zörmän näm"),
            LocalizedName("th-Latn", "ziämäs näm in Lätin"),
            LocalizedName("guj", "güjäräti näm")
        ).applyTo(builder)
        val map = mutableMapOf<String, String>()
        builder.create().applyTo(map)

        assertEquals(
            mapOf(
                "name" to "näm",
                "int_name" to "ünt_näm",
                "name:de" to "zörmän näm",
                "name:th-Latn" to "ziämäs näm in Lätin",
                "name:guj" to "güjäräti näm"
            ),
            map
        )
    }
}
