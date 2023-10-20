package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import org.assertj.core.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test fun `apply empty localized names`() {
        verifyAnswer(
            mapOf(),
            listOf(),
            arrayOf()
        )
    }

    @Test fun `apply empty localized names removes previously set names`() {
        verifyAnswer(
            mapOf(
                "name" to "1",
                "int_name" to "2",
                "name:de" to "3",
                "name:th-Latn" to "4",
                "name:guj" to "5"
            ),
            listOf(),
            arrayOf(
                StringMapEntryDelete("name", "1"),
                StringMapEntryDelete("int_name", "2"),
                StringMapEntryDelete("name:de", "3"),
                StringMapEntryDelete("name:th-Latn", "4"),
                StringMapEntryDelete("name:guj", "5")
            )
        )
    }

    @Test fun `apply empty localized names does not remove noname and variants`() {
        verifyAnswer(
            mapOf(
                "noname" to "yes",
                "name:signed" to "no"
            ),
            listOf(),
            arrayOf()
        )
    }

    @Test fun `apply localized names`() {
        verifyAnswer(
            mapOf(),
            listOf(
                LocalizedName("", "1"),
                LocalizedName("international", "2"),
                LocalizedName("de", "3"),
                LocalizedName("th-Latn", "4"),
                LocalizedName("guj", "5")
            ),
            arrayOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryAdd("int_name", "2"),
                StringMapEntryAdd("name:de", "3"),
                StringMapEntryAdd("name:th-Latn", "4"),
                StringMapEntryAdd("name:guj", "5")
            )
        )
    }

    @Test fun `apply localized names removes noname and variants`() {
        verifyAnswer(
            mapOf("noname" to "yes", "name:signed" to "no"),
            listOf(LocalizedName("", "1")),
            arrayOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryDelete("noname", "yes"),
                StringMapEntryDelete("name:signed", "no"),
            )
        )
    }

    @Test fun `apply one name only drops language tag`() {
        verifyAnswer(
            mapOf("name:de" to "hoho"),
            listOf(LocalizedName("de", "1")),
            arrayOf(
                StringMapEntryAdd("name", "1"),
                StringMapEntryDelete("name:de", "hoho"),
            )
        )
    }

    @Test fun `apply two names adds name tag`() {
        verifyAnswer(
            mapOf(),
            listOf(
                LocalizedName("de", "haha"),
                LocalizedName("es", "jaja")
            ),
            arrayOf(
                StringMapEntryAdd("name", "haha"),
                StringMapEntryAdd("name:de", "haha"),
                StringMapEntryAdd("name:es", "jaja"),
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: List<LocalizedName>, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
