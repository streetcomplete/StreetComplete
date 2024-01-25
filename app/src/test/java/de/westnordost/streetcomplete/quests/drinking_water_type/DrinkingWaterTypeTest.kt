package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.osm.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class DrinkingWaterTypeTest {
    @Test fun `set drinking water as spring`() {
        assertEquals(
            setOf(StringMapEntryAdd("natural", "spring")),
            DrinkingWaterType.SPRING.appliedTo(mapOf("amenity" to "drinking_water"))
        )
    }

    @Test fun `set drinking water as water tap`() {
        assertEquals(
            setOf(StringMapEntryAdd("man_made", "water_tap")),
            DrinkingWaterType.WATER_TAP.appliedTo(mapOf("amenity" to "drinking_water"))
        )
    }

    @Test fun `set drinking water as bubbler fountain`() {
        assertEquals(
            setOf(StringMapEntryAdd("fountain", "bubbler")),
            DrinkingWaterType.WATER_FOUNTAIN_JET.appliedTo(mapOf("amenity" to "drinking_water"))
        )
    }

    @Test fun `set drinking water as bottle refill`() {
        assertEquals(
            setOf(StringMapEntryAdd("fountain", "bottle_refill")),
            DrinkingWaterType.WATER_FOUNTAIN_BOTTLE_REFILL_ONLY.appliedTo(mapOf(
                "amenity" to "drinking_water"
            ))
        )
    }

    @Test fun `set drinking water as fountain`() {
        assertEquals(
            setOf(StringMapEntryAdd("fountain", "drinking")),
            DrinkingWaterType.WATER_FOUNTAIN_GENERIC.appliedTo(mapOf(
                "amenity" to "drinking_water"
            ))
        )
    }

    @Test fun `set drinking water as water well`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("man_made", "water_well"),
                StringMapEntryAdd("pump", "manual"),
            ),
            DrinkingWaterType.HAND_PUMP.appliedTo(mapOf("amenity" to "drinking_water"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("man_made", "water_well"),
                StringMapEntryAdd("pump", "no"),
            ),
            DrinkingWaterType.WATER_WELL_WITHOUT_PUMP.appliedTo(mapOf(
                "amenity" to "drinking_water"
            ))
        )
    }

    @Test fun `set formerly disused drinking water as water tap`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("man_made", "water_tap"),
                StringMapEntryAdd("amenity", "drinking_water"),
                StringMapEntryDelete("disused:amenity", "drinking_water"),
            ),
            DrinkingWaterType.WATER_TAP.appliedTo(mapOf("disused:amenity" to "drinking_water"))
        )
    }

    @Test fun `set as disused`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("amenity", "drinking_water"),
                StringMapEntryAdd("disused:amenity", "drinking_water"),
            ),
            DrinkingWaterType.DISUSED_DRINKING_WATER.appliedTo(mapOf("amenity" to "drinking_water"))
        )
    }

    @Test fun `keep as disused`() {
        assertEquals(
            setOf(
                StringMapEntryAdd(SURVEY_MARK_KEY, nowAsCheckDateString()),
            ),
            DrinkingWaterType.DISUSED_DRINKING_WATER.appliedTo(mapOf(
                "disused:amenity" to "drinking_water"
            ))
        )
    }
}

private fun DrinkingWaterType.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
