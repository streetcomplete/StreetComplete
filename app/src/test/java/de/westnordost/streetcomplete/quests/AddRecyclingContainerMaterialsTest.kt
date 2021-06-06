package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterials
import de.westnordost.streetcomplete.quests.recycling_material.IsWasteContainer
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*
import de.westnordost.streetcomplete.util.translate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AddRecyclingContainerMaterialsTest {

    private val questType = AddRecyclingContainerMaterials()

    @Test fun `applicable to container without recycling materials`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to container with recycling materials`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container",
                "recycling:something" to "yes"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to container with old recycling materials`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container",
                "check_date:recycling" to "2001-01-01",
                "recycling:plastic_packaging" to "yes",
                "recycling:something_else" to "no"
            ), timestamp = Instant.now().toEpochMilli())
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to container with old but unknown recycling materials`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container",
                "check_date:recycling" to "2001-01-01",
                "recycling:something_else" to "yes"
            ), timestamp = Instant.now().toEpochMilli())
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to container without recycling materials close to another container`() {
        val pos1 = p(0.0,0.0)
        val pos2 = pos1.translate(19.0, 45.0)

        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, pos = pos1, tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container"
            )),
            node(id = 2, pos = pos2, tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to container without recycling materials not too close to another container`() {
        val pos1 = p(0.0,0.0)
        val pos2 = pos1.translate(21.0, 45.0)

        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, pos = pos1, tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container"
            )),
            node(id = 2, pos = pos2, tags = mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container",
                "recycling:paper" to "yes"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

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
            StringMapEntryAdd("recycling:beverage_cartons", "no"),
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
            StringMapEntryAdd("recycling:beverage_cartons", "no"),
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
            StringMapEntryAdd("recycling:beverage_cartons", "no"),
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
            StringMapEntryAdd("check_date:recycling", LocalDate.now().toCheckDateString())
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

    @Test fun `apply answer updates previous check dates`() {
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
            StringMapEntryModify("check_date:recycling", "2000-11-02", LocalDate.now().toCheckDateString()),
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
