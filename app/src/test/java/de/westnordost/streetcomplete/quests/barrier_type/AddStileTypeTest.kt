package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddStileTypeTest {
    private val questType = AddStileType()

    @Test
    fun `set stile as squeezer`() {
        assertEquals(
            setOf(StringMapEntryAdd("stile", "squeezer")),
            questType.answerAppliedTo(StileType.SQUEEZER, mapOf("barrier" to "stile"))
        )
    }

    @Test
    fun `set stile as still squeezer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("check_date", nowAsCheckDateString()),
                StringMapEntryModify("stile", "squeezer", "squeezer"),
            ),
            questType.answerAppliedTo(
                StileType.SQUEEZER,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "squeezer",
                )
            )
        )
    }

    @Test
    fun `reset tags when marking stile as a rebuilt stepover`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("steps", "5"),
                StringMapEntryModify("material", "wood", "stone"),
                StringMapEntryModify("stile", "stepover", "stepover"),
            ),
            questType.answerAppliedTo(
                StileType.STEPOVER_STONE,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "stepover",
                    "material" to "wood",
                    "steps" to "5",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `reset tags when stile has been rebuilt as another kind of stile`() {
        assertEquals(
            setOf(
                StringMapEntryModify("stile", "stepover", "squeezer"),
                StringMapEntryDelete("steps", "5"),
                StringMapEntryDelete("material", "wood"),
            ),
            questType.answerAppliedTo(
                StileType.SQUEEZER,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "stepover",
                    "material" to "wood",
                    "steps" to "5",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `don't reset tags when adding a stile type tag to an existing enhanced metadata stile`() {
        assertEquals(
            setOf(StringMapEntryAdd("stile", "squeezer")),
            questType.answerAppliedTo(
                StileType.SQUEEZER,
                mapOf(
                    "barrier" to "stile",
                    "material" to "stone",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `don't reset tags when adding a stile type tag to a stile where the chosen material is already tagged`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("stile", "stepover"),
                StringMapEntryModify("material", "wood", "wood")
            ),
            questType.answerAppliedTo(
                StileType.STEPOVER_WOODEN,
                mapOf(
                    "barrier" to "stile",
                    "material" to "wood",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `handle adding stile tag where other values are present`() {
        assertEquals(
            setOf(StringMapEntryAdd("stile", "squeezer")),
            questType.answerAppliedTo(
                StileType.SQUEEZER,
                mapOf(
                    "barrier" to "stile",
                    "material" to "stone",
                    "steps" to "3",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `handle unmodified, well tagged stepover`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("check_date", nowAsCheckDateString()),
                StringMapEntryModify("stile", "stepover", "stepover"),
                StringMapEntryModify("material", "wood", "wood"),
            ),
            questType.answerAppliedTo(
                StileType.STEPOVER_WOODEN,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "stepover",
                    "material" to "wood",
                    "steps" to "5",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }

    @Test
    fun `on stepover add material if everything else is tagged`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("material", "wood"),
                StringMapEntryModify("stile", "stepover", "stepover"),
            ),
            questType.answerAppliedTo(
                StileType.STEPOVER_WOODEN,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "stepover",
                )
            )
        )
    }

    @Test
    fun `answering that it is now a kissing gate`() {
        assertEquals(
            setOf(
                StringMapEntryModify("barrier", "stile", "kissing_gate"),
            ),
            questType.answerAppliedTo(
                ConvertedStile.KISSING_GATE,
                mapOf("barrier" to "stile")
            )
        )
    }

    @Test
    fun `answering that it is now a kissing gate removes any stile properties`() {
        assertEquals(
            setOf(
                StringMapEntryModify("barrier", "stile", "kissing_gate"),
                StringMapEntryDelete("stile", "something"),
                StringMapEntryDelete("material", "something else"),
            ),
            questType.answerAppliedTo(
                ConvertedStile.KISSING_GATE,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "something",
                    "material" to "something else",
                    "ref" to "123",
                )
            )
        )
    }

    @Test
    fun `reset tags when marking stile as a passage`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("steps", "5"),
                StringMapEntryDelete("material", "wood"),
                StringMapEntryDelete("stile", "stepover"),
                StringMapEntryModify("barrier", "stile", "entrance"),
            ),
            questType.answerAppliedTo(
                ConvertedStile.PASSAGE,
                mapOf(
                    "barrier" to "stile",
                    "stile" to "stepover",
                    "material" to "wood",
                    "steps" to "5",
                    "tag_not_in_list_for_removal" to "dummy_value",
                )
            )
        )
    }
}
