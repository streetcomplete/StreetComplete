package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mock
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CheckExistenceTest {
    private val questType = CheckExistence(mock())

    @Test fun `apply answer adds check date`() {
        questType.verifyAnswer(
            Unit,
            StringMapEntryAdd("check_date", LocalDate.now().toCheckDateString())
        )
    }

    @Test fun `apply answer removes all previous survey keys`() {
        questType.verifyAnswer(
            mapOf(
                "check_date" to "1",
                "lastcheck" to "a",
                "last_checked" to "b",
                "survey:date" to "c",
                "survey_date" to "d"
            ),
            Unit,
            StringMapEntryModify("check_date", "1", LocalDate.now().toCheckDateString()),
            StringMapEntryDelete("lastcheck", "a"),
            StringMapEntryDelete("last_checked", "b"),
            StringMapEntryDelete("survey:date", "c"),
            StringMapEntryDelete("survey_date", "d"),
        )
    }

    @Test fun `title is constructed correctly, issue #2512`() {
        val newspaperVendingMachineWithName = questType.getQuestTitle(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "name" to "Bild",
        ))
        assertEquals(newspaperVendingMachineWithName, "Is Bild (vending machine) still here?")

        val newspaperVendingMachineWithBrand = questType.getQuestTitle(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "brand" to "Abendzeitung",
        ))
        assertEquals(newspaperVendingMachineWithBrand, "Is Abendzeitung (vending machine) still here?")
    }

    @Test fun `title is constructed correctly, issue #2640`() {
        val postBox = questType.getQuestTitle(mapOf(
            "amenity" to "post_box",
            "brand" to "Deutsche Post",
            "operator" to "Deutsche Post AG",
            "ref" to "Hauptsmoorstr. 101, 96052 Bamberg",
        ))
        assertEquals(postBox, "Is Deutsche Post (post box) still here?")
    }

    @Test fun `title is constructed correctly, issue #2806`() {
        val namedBench = questType.getQuestTitle(mapOf(
            "amenity" to "bench",
            "name" to "Sergey's Seat",
            "ref" to "600913",
            "brand" to "Google",
            "operator" to "Google RESTful",
        ))
        assertEquals(namedBench, "Is Sergey's Seat (bench) still here?")

        val unnamedBench = questType.getQuestTitle(mapOf(
            "amenity" to "bench",
        ))
        assertEquals(unnamedBench, "Is this still here? (bench)")
    }
}

private fun CheckExistence.getQuestTitle(tags: Map<String, String>): String {
    val titleTemplate = when (getTitle(tags)) {
        R.string.quest_existence_name_title -> "Is %1\$s (%2\$s) still here?"
        R.string.quest_existence_title -> "Is this still here? (%s)"
        else -> throw IllegalArgumentException("Unknown title resource")
    }

    // simplified feature name, instead of looking up iD preset
    val featureName = tags["amenity"]?.replace('_', ' ')

    val titleArgs = getTitleArgs(tags, lazy { featureName })

    return titleTemplate.format(*titleArgs)
}
