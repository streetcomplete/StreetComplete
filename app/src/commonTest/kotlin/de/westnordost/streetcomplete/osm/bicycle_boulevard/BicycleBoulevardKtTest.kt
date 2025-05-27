package de.westnordost.streetcomplete.osm.bicycle_boulevard

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BicycleBoulevardKtTest {

    @Test fun create() {
        assertEquals(NO, parseBicycleBoulevard(mapOf()))
        assertEquals(NO, parseBicycleBoulevard(mapOf("bicycle_road" to "no")))
        assertEquals(YES, parseBicycleBoulevard(mapOf("bicycle_road" to "yes")))
        assertEquals(NO, parseBicycleBoulevard(mapOf("cyclestreet" to "no")))
        assertEquals(YES, parseBicycleBoulevard(mapOf("cyclestreet" to "yes")))
    }

    @Test fun `apply yes when it was not tagged before`() {
        assertEquals(setOf(StringMapEntryAdd("bicycle_road", "yes")), YES.appliedTo(mapOf(), "DE"))
        assertEquals(setOf(StringMapEntryAdd("bicycle_road", "yes")), YES.appliedTo(mapOf(), "US"))

        assertEquals(setOf(StringMapEntryAdd("cyclestreet", "yes")), YES.appliedTo(mapOf(), "BE"))
        assertEquals(setOf(StringMapEntryAdd("cyclestreet", "yes")), YES.appliedTo(mapOf(), "NL"))
        assertEquals(setOf(StringMapEntryAdd("cyclestreet", "yes")), YES.appliedTo(mapOf(), "LU"))
    }

    @Test fun `apply yes when it was tagged before`() {
        // modifying current tag
        assertEquals(
            setOf(StringMapEntryModify("bicycle_road", "no", "yes")),
            YES.appliedTo(mapOf("bicycle_road" to "no"), "DE"),
        )
        assertEquals(
            setOf(StringMapEntryModify("cyclestreet", "no", "yes")),
            YES.appliedTo(mapOf("cyclestreet" to "no"), "NL"),
        )

        // keeping current tag in country where that tag is not usually used
        assertEquals(
            setOf(StringMapEntryModify("cyclestreet", "no", "yes")),
            YES.appliedTo(mapOf("cyclestreet" to "no"), "DE"),
        )
        assertEquals(
            setOf(StringMapEntryModify("bicycle_road", "no", "yes")),
            YES.appliedTo(mapOf("bicycle_road" to "no"), "NL"),
        )
    }

    @Test fun `apply no`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("bicycle_road", "yes"),
                StringMapEntryDelete("cyclestreet", "yes"),
            ),
            NO.appliedTo(mapOf(
                "bicycle_road" to "yes",
                "cyclestreet" to "yes"
            ), "DE")
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("bicycle_road", "yes"),
                StringMapEntryDelete("cyclestreet", "yes"),
            ),
            NO.appliedTo(mapOf(
                "bicycle_road" to "yes",
                "cyclestreet" to "yes"
            ), "DE")
        )
    }
}

private fun BicycleBoulevard.appliedTo(tags: Map<String, String>, countryCode: String): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, countryCode)
    return cb.create().changes
}
