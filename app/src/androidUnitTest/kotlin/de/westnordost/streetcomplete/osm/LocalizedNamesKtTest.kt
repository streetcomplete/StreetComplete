package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LocalizedNamesKtTest {

    @Test fun parseLocalizedNames() {
        assertEquals(
            listOf(
                LocalizedName("", "näm"), // name at first position
                LocalizedName("international", "ünt_näm"),
                LocalizedName("de", "zörmän näm"),
                LocalizedName("th-Latn", "ziämäs näm in Lätin"),
                LocalizedName("guj", "güjäräti näm")
            ),
            parseLocalizedNames(mapOf(
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

    @Test fun `apply empty localized names`() {
        assertEquals(
            setOf(),
            listOf<LocalizedName>().appliedTo(mapOf()),
        )
    }

    @Test fun `apply empty localized names removes previously set names`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("name", "1"),
                StringMapEntryDelete("int_name", "2"),
                StringMapEntryDelete("name:de", "3"),
                StringMapEntryDelete("name:th-Latn", "4"),
                StringMapEntryDelete("name:guj", "5")
            ),
            listOf<LocalizedName>().appliedTo(mapOf(
                "name" to "1",
                "int_name" to "2",
                "name:de" to "3",
                "name:th-Latn" to "4",
                "name:guj" to "5"
            ))
        )
    }

    @Test fun `apply empty localized names does not remove noname and variants`() {
        assertEquals(
            setOf(),
            listOf<LocalizedName>().appliedTo(mapOf(
                "noname" to "yes",
                "name:signed" to "no"
            ))
        )
    }

    @Test fun `apply localized names`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryAdd("int_name", "2"),
                StringMapEntryAdd("name:de", "3"),
                StringMapEntryAdd("name:th-Latn", "4"),
                StringMapEntryAdd("name:guj", "5")
            ),
            listOf(
                LocalizedName("", "1"),
                LocalizedName("international", "2"),
                LocalizedName("de", "3"),
                LocalizedName("th-Latn", "4"),
                LocalizedName("guj", "5")
            ).appliedTo(mapOf())
        )
    }

    @Test fun `apply localized names removes noname and variants`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryDelete("noname", "yes"),
                StringMapEntryDelete("name:signed", "no"),
            ),
            listOf(LocalizedName("", "1")).appliedTo(mapOf(
                "noname" to "yes",
                "name:signed" to "no"
            )),
        )
    }

    @Test fun `apply one name only drops language tag`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryDelete("name:de", "hoho"),
            ),
            listOf(LocalizedName("de", "1")).appliedTo(mapOf("name:de" to "hoho")),
        )
    }

    @Test fun `apply two names adds name tag`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "haha"),
                StringMapEntryAdd("name:de", "haha"),
                StringMapEntryAdd("name:es", "jaja"),
            ),
            listOf(
                LocalizedName("de", "haha"),
                LocalizedName("es", "jaja")
            ).appliedTo(mapOf())
        )
    }
}

private fun List<LocalizedName>.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
