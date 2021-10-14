package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import org.junit.Test
import java.time.LocalDate
import de.westnordost.streetcomplete.quests.verifyAnswer

class AddStileTypeTest {
    private val questType = AddStileType()

    @Test
    fun `set stile as squeezer`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
            ),
            BarrierType.STILE_SQUEEZER,
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
            BarrierType.STILE_SQUEEZER,
            StringMapEntryAdd("check_date:stile", LocalDate.now().toCheckDateString()),
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
            BarrierType.STILE_STEPOVER_STONE,
            StringMapEntryDelete("steps", "5"),
            StringMapEntryModify("material", "wood", "stone"),
        )
    }

    @Test
    fun `don't reset tags when adding a stile type tag to an existing enhanced metadata stile`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "steps" to "3",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            BarrierType.STILE_STEPOVER_STONE,
            StringMapEntryAdd("stile", "stepover"),
            StringMapEntryAdd("material", "stone"),
        )
    }

    @Test
    fun `handle unmodified, fully tagged stepover`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
                "stile" to "stepover",
                "material" to "wood",
                "steps" to "5",
                "tag_not_in_list_for_removal" to "dummy_value",
            ),
            BarrierType.STILE_STEPOVER_WOODEN,
            StringMapEntryAdd("check_date:stile", LocalDate.now().toCheckDateString()),
        )
    }
}
