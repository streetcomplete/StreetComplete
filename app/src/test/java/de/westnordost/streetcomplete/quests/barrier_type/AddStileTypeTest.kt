package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import kotlin.test.Test

class AddStileTypeTest {
    private val questType = AddStileType()

    @Test
    fun `set stile as squeezer`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
            ),
            StileType.SQUEEZER,
            StringMapEntryAdd("stile", "squeezer"),
        )
    }

    @Test
    fun `set stile as still squeezer`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "squeezer",
            ),
            StileType.SQUEEZER,
            StringMapEntryAdd("check_date", nowAsCheckDateString()),
            StringMapEntryModify("stile", "squeezer", "squeezer"),
        )
    }

    @Test
    fun `reset tags when marking stile as a rebuilt stepover`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
                "material" to "wood",
                "steps" to "5",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.STEPOVER_STONE,
            StringMapEntryDelete("steps", "5"),
            StringMapEntryModify("material", "wood", "stone"),
            StringMapEntryModify("stile", "stepover", "stepover"),
        )
    }

    @Test
    fun `reset tags when stile has been rebuilt as another kind of stile`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
                "material" to "wood",
                "steps" to "5",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.SQUEEZER,
            StringMapEntryModify("stile", "stepover", "squeezer"),
            StringMapEntryDelete("steps", "5"),
            StringMapEntryDelete("material", "wood"),
        )
    }

    @Test
    fun `don't reset tags when adding a stile type tag to an existing enhanced metadata stile`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "material" to "stone",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.SQUEEZER,
            StringMapEntryAdd("stile", "squeezer"),
        )
    }

    @Test
    fun `don't reset tags when adding a stile type tag to a stile where the chosen material is already tagged`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "material" to "wood",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.STEPOVER_WOODEN,
            StringMapEntryAdd("stile", "stepover"),
            StringMapEntryModify("material", "wood", "wood"),
        )
    }

    @Test
    fun `handle adding stile tag where other values are present`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "material" to "stone",
                "steps" to "3",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.SQUEEZER,
            StringMapEntryAdd("stile", "squeezer"),
        )
    }

    @Test
    fun `handle unmodified, well tagged stepover`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
                "material" to "wood",
                "steps" to "5",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            StileType.STEPOVER_WOODEN,
            StringMapEntryAdd("check_date", nowAsCheckDateString()),
            StringMapEntryModify("stile", "stepover", "stepover"),
            StringMapEntryModify("material", "wood", "wood"),

        )
    }

    @Test
    fun `on stepover add material if everything else is tagged`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
            ),
            StileType.STEPOVER_WOODEN,
            StringMapEntryAdd("material", "wood"),
            StringMapEntryModify("stile", "stepover", "stepover"),
        )
    }

    @Test
    fun `answering that it is now a kissing gate`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile"
            ),
            ConvertedStile.KISSING_GATE,
            StringMapEntryModify("barrier", "stile", "kissing_gate"),
        )
    }

    @Test
    fun `answering that it is now a kissing gate removes any stile properties`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "something",
                "material" to "something else",
                "ref" to "123",
            ),
            ConvertedStile.KISSING_GATE,
            StringMapEntryModify("barrier", "stile", "kissing_gate"),
            StringMapEntryDelete("stile", "something"),
            StringMapEntryDelete("material", "something else"),
        )
    }

    @Test
    fun `reset tags when marking stile as a passage`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
                "material" to "wood",
                "steps" to "5",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            ConvertedStile.PASSAGE,
            StringMapEntryDelete("steps", "5"),
            StringMapEntryDelete("material", "wood"),
            StringMapEntryDelete("stile", "stepover"),
            StringMapEntryModify("barrier", "stile", "entrance"),
        )
    }
}
