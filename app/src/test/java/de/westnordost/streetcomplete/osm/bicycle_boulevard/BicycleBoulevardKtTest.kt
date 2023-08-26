package de.westnordost.streetcomplete.osm.bicycle_boulevard

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard.*
import org.assertj.core.api.Assertions
import kotlin.test.*
import kotlin.test.Test

class BicycleBoulevardKtTest {

    @Test fun create() {
        assertEquals(NO, createBicycleBoulevard(mapOf()))
        assertEquals(NO, createBicycleBoulevard(mapOf("bicycle_road" to "no")))
        assertEquals(YES, createBicycleBoulevard(mapOf("bicycle_road" to "yes")))
        assertEquals(NO, createBicycleBoulevard(mapOf("cyclestreet" to "no")))
        assertEquals(YES, createBicycleBoulevard(mapOf("cyclestreet" to "yes")))
    }

    @Test fun `apply yes when it was not tagged before`() {
        verifyAnswer(mapOf(), YES, "DE", arrayOf(StringMapEntryAdd("bicycle_road", "yes")))
        verifyAnswer(mapOf(), YES, "US", arrayOf(StringMapEntryAdd("bicycle_road", "yes")))

        verifyAnswer(mapOf(), YES, "BE", arrayOf(StringMapEntryAdd("cyclestreet", "yes")))
        verifyAnswer(mapOf(), YES, "NL", arrayOf(StringMapEntryAdd("cyclestreet", "yes")))
        verifyAnswer(mapOf(), YES, "LU", arrayOf(StringMapEntryAdd("cyclestreet", "yes")))
    }

    @Test fun `apply yes when it was tagged before`() {
        // modifying current tag
        verifyAnswer(
            mapOf("bicycle_road" to "no"),
            YES, "DE",
            arrayOf(StringMapEntryModify("bicycle_road", "no", "yes"))
        )
        verifyAnswer(
            mapOf("cyclestreet" to "no"),
            YES, "NL",
            arrayOf(StringMapEntryModify("cyclestreet", "no", "yes"))
        )

        // keeping current tag in country where that tag is not usually used
        verifyAnswer(
            mapOf("cyclestreet" to "no"),
            YES, "DE",
            arrayOf(StringMapEntryModify("cyclestreet", "no", "yes"))
        )
        verifyAnswer(
            mapOf("bicycle_road" to "no"),
            YES, "NL",
            arrayOf(StringMapEntryModify("bicycle_road", "no", "yes"))
        )
    }

    @Test fun `apply no`() {
        verifyAnswer(
            mapOf("bicycle_road" to "yes", "cyclestreet" to "yes"),
            NO, "DE",
            arrayOf(
                StringMapEntryDelete("bicycle_road", "yes"),
                StringMapEntryDelete("cyclestreet", "yes"),
            )
        )
        verifyAnswer(
            mapOf("bicycle_road" to "yes", "cyclestreet" to "yes"),
            NO, "DE",
            arrayOf(
                StringMapEntryDelete("bicycle_road", "yes"),
                StringMapEntryDelete("cyclestreet", "yes"),
            )
        )
    }
}

private fun verifyAnswer(
    tags: Map<String, String>,
    answer: BicycleBoulevard,
    countryCode: String,
    expectedChanges: Array<StringMapEntryChange>
) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb, countryCode)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
