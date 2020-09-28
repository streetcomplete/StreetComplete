package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterials
import de.westnordost.streetcomplete.quests.recycling_material.IsWasteContainer
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyInt
import java.util.*

class AddRecyclingContainerMaterialsTest {

    @Before fun setUp() {
        val r: ResurveyIntervalsStore = mock()
        on(r.times(anyInt())).thenAnswer { (it.arguments[0] as Int).toDouble() }
        on(r.times(anyDouble())).thenAnswer { (it.arguments[0] as Double) }
        questType = AddRecyclingContainerMaterials(mock(), r)
    }

    private lateinit var questType: AddRecyclingContainerMaterials

    @Test fun `apply normal answer`() {
        questType.verifyAnswer(
            RecyclingMaterials(listOf(SHOES, PAPER)),
            StringMapEntryAdd("recycling:shoes", "yes"),
            StringMapEntryAdd("recycling:paper", "yes")
        )
    }

    @Test fun `apply answer with plastic bottles`() {
        questType.verifyAnswer(
            RecyclingMaterials(listOf(PLASTIC_BOTTLES)),
            StringMapEntryAdd("recycling:plastic_bottles", "yes"),
            StringMapEntryAdd("recycling:plastic_packaging", "no"),
            StringMapEntryAdd("recycling:plastic", "no")
        )
    }

    @Test fun `apply answer with plastic bottles and previous plastic answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic" to "yes"
            ),
            RecyclingMaterials(listOf(PLASTIC_BOTTLES)),
            StringMapEntryAdd("recycling:plastic_bottles", "yes"),
            StringMapEntryAdd("recycling:plastic_packaging", "no"),
            StringMapEntryModify("recycling:plastic", "yes", "no")
        )
    }

    @Test fun `apply answer with plastic bottles and previous plastic packaging answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic" to "no",
                "recycling:plastic_packaging" to "yes"
            ),
            RecyclingMaterials(listOf(PLASTIC_BOTTLES)),
            StringMapEntryModify("recycling:plastic", "no", "no"),
            StringMapEntryAdd("recycling:plastic_bottles", "yes"),
            StringMapEntryModify("recycling:plastic_packaging", "yes", "no")
        )
    }

    @Test fun `apply answer with plastic packaging`() {
        questType.verifyAnswer(
            RecyclingMaterials(listOf(PLASTIC_PACKAGING)),
            StringMapEntryAdd("recycling:plastic_packaging", "yes"),
            StringMapEntryAdd("recycling:plastic", "no")
        )
    }

    @Test fun `apply answer with plastic packaging and previous plastic answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic" to "yes"
            ),
            RecyclingMaterials(listOf(PLASTIC_PACKAGING)),
            StringMapEntryAdd("recycling:plastic_packaging", "yes"),
            StringMapEntryModify("recycling:plastic", "yes", "no")
        )
    }

    @Test fun `apply answer with plastic packaging and previous plastic bottles answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic_bottles" to "yes",
                "recycling:plastic_packaging" to "no",
                "recycling:plastic" to "no"
            ),
            RecyclingMaterials(listOf(PLASTIC_PACKAGING)),
            StringMapEntryModify("recycling:plastic_packaging", "no", "yes"),
            StringMapEntryDelete("recycling:plastic_bottles", "yes"),
            StringMapEntryModify("recycling:plastic", "no", "no")
        )
    }

    @Test fun `apply answer with plastic`() {
        questType.verifyAnswer(
            RecyclingMaterials(listOf(PLASTIC)),
            StringMapEntryAdd("recycling:plastic", "yes")
        )
    }

    @Test fun `apply answer with plastic and previous plastic bottles answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic_bottles" to "yes",
                "recycling:plastic_packaging" to "no",
                "recycling:plastic" to "no"
            ),
            RecyclingMaterials(listOf(PLASTIC)),
            StringMapEntryModify("recycling:plastic", "no", "yes"),
            StringMapEntryDelete("recycling:plastic_bottles", "yes"),
            StringMapEntryDelete("recycling:plastic_packaging", "no")
        )
    }

    @Test fun `apply answer with plastic and previous plastic packaging answer`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:plastic" to "no",
                "recycling:plastic_packaging" to "yes"
            ),
            RecyclingMaterials(listOf(PLASTIC)),
            StringMapEntryModify("recycling:plastic", "no", "yes"),
            StringMapEntryDelete("recycling:plastic_packaging", "yes")
        )
    }

    @Test fun `apply answer with same answers as before`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:paper" to "yes",
                "recycling:clothes" to "yes"
            ),
            RecyclingMaterials(listOf(CLOTHES, PAPER)),
            StringMapEntryModify("recycling:paper", "yes", "yes"),
            StringMapEntryModify("recycling:clothes", "yes", "yes"),
            StringMapEntryAdd("check_date:recycling", Date().toCheckDateString())
        )
    }

    @Test fun `apply answer removes previous yes-answers`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:paper" to "yes",
                "recycling:cooking_oil" to "yes",
                "recycling:green_waste" to "yes"
            ),
            RecyclingMaterials(listOf(SHOES, PAPER)),
            StringMapEntryAdd("recycling:shoes", "yes"),
            StringMapEntryModify("recycling:paper", "yes", "yes"),
            StringMapEntryDelete("recycling:cooking_oil", "yes"),
            StringMapEntryDelete("recycling:green_waste", "yes")
        )
    }

    @Test fun `apply answer removes previous check dates`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:paper" to "no",
                "recycling:check_date" to "2000-11-01",
                "check_date:recycling" to "2000-11-02",
                "recycling:lastcheck" to "2000-11-03",
                "lastcheck:recycling" to "2000-11-04",
                "recycling:last_checked" to "2000-11-05",
                "last_checked:recycling" to "2000-11-06"
            ),
            RecyclingMaterials(listOf(PAPER)),
            StringMapEntryModify("recycling:paper", "no", "yes"),
            StringMapEntryDelete("recycling:check_date", "2000-11-01"),
            StringMapEntryDelete("check_date:recycling", "2000-11-02"),
            StringMapEntryDelete("recycling:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:recycling", "2000-11-04"),
            StringMapEntryDelete("recycling:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:recycling", "2000-11-06")
        )
    }

    @Test fun `apply answer doesn't touch previous no-answers`() {
        questType.verifyAnswer(
            mapOf(
                "recycling:cooking_oil" to "no",
                "recycling:green_waste" to "no"
            ),
            RecyclingMaterials(listOf(PAPER)),
            StringMapEntryAdd("recycling:paper", "yes")
        )
    }

    @Test fun `apply waste answer`() {
        questType.verifyAnswer(
            mapOf("amenity" to "recycling", "recycling_type" to "container"),
            IsWasteContainer,
            StringMapEntryModify("amenity","recycling","waste_disposal"),
            StringMapEntryDelete("recycling_type", "container")
        )
    }

    @Test fun `apply waste answer deletes check dates for recycling and any previous recycling keys`() {
        questType.verifyAnswer(
            mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container",
                "recycling:check_date" to "2000-11-11",
                "check_date:recycling" to "2000-11-11",
                "recycling:lastcheck" to "2000-11-11",
                "lastcheck:recycling" to "2000-11-11",
                "recycling:last_checked" to "2000-11-11",
                "last_checked:recycling" to "2000-11-11",
                "recycling:something" to "yes",
                "recycling:another_thing" to "no"
            ),
            IsWasteContainer,
            StringMapEntryModify("amenity","recycling","waste_disposal"),
            StringMapEntryDelete("recycling_type", "container"),
            StringMapEntryDelete("check_date:recycling", "2000-11-11"),
            StringMapEntryDelete("recycling:check_date", "2000-11-11"),
            StringMapEntryDelete("recycling:lastcheck", "2000-11-11"),
            StringMapEntryDelete("lastcheck:recycling", "2000-11-11"),
            StringMapEntryDelete("recycling:last_checked", "2000-11-11"),
            StringMapEntryDelete("last_checked:recycling", "2000-11-11"),
            StringMapEntryDelete("recycling:something", "yes"),
            StringMapEntryDelete("recycling:another_thing", "no")
        )
    }

}
