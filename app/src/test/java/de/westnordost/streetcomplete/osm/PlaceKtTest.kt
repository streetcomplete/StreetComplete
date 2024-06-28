package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceKtTest {

    @Test fun `replacePlace removes all previous survey keys`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("a", "b"),
                StringMapEntryDelete("check_date", "1"),
                StringMapEntryDelete("lastcheck", "a"),
                StringMapEntryDelete("last_checked", "b"),
                StringMapEntryDelete("survey:date", "c"),
                StringMapEntryDelete("survey_date", "d"),
            ),
            replacePlaceApplied(
                newTags = mapOf("a" to "b"),
                oldTags = mapOf(
                    "check_date" to "1",
                    "lastcheck" to "a",
                    "last_checked" to "b",
                    "survey:date" to "c",
                    "survey_date" to "d"
                )
            )
        )
    }

    // see KEYS_THAT_SHOULD_BE_REMOVED_WHEN_PLACE_IS_REPLACED
    @Test fun `replacePlace removes certain tags connected with the type of place`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("shop", "ice_cream"),
                StringMapEntryDelete("disused:amenity", "yes"),
                StringMapEntryDelete("phone", "123456"),
                StringMapEntryDelete("name", "Juppiebude"),
                StringMapEntryDelete("ref", "1111"),
                StringMapEntryDelete("fee", "yes"),
                StringMapEntryDelete("office", "it"),
                StringMapEntryDelete("tourism", "information"),
                StringMapEntryDelete("information", "office"),
            ),
            replacePlaceApplied(
                newTags = mapOf("shop" to "ice_cream"),
                oldTags = mapOf(
                    "building" to "yes", // <- should not be cleared
                    "disused:amenity" to "yes",
                    "phone" to "123456",
                    "ref" to "1111",
                    "fee" to "yes",
                    "nycdoitt:bin" to "22222",
                    "barrier" to "wall",
                    "office" to "it",
                    "tourism" to "information",
                    "information" to "office",
                    "name" to "Juppiebude"
                )
            )
        )
    }
}

private fun replacePlaceApplied(newTags: Map<String, String>, oldTags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(oldTags)
    cb.replacePlace(newTags)
    return cb.create().changes
}
