package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.CLOTHES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PAPER
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PET
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_BOTTLES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_PACKAGING
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.SHOES
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals

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
            ), timestamp = nowAsEpochMilliseconds())
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
            ), timestamp = nowAsEpochMilliseconds())
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply normal answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:shoes", "yes"),
                StringMapEntryAdd("recycling:paper", "yes")
            ),
            questType.answerApplied(RecyclingMaterials(listOf(SHOES, PAPER)))
        )
    }

    @Test fun `apply answer with plastic`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:plastic", "yes")
            ),
            questType.answerApplied(RecyclingMaterials(listOf(PLASTIC)))
        )
    }

    @Test fun `apply answer with plastic packaging`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:plastic_packaging", "yes"),
                StringMapEntryAdd("recycling:plastic", "no")
            ),
            questType.answerApplied(RecyclingMaterials(listOf(PLASTIC_PACKAGING)))
        )
    }

    @Test fun `apply answer with plastic bottles`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:plastic_bottles", "yes"),
                StringMapEntryAdd("recycling:plastic_packaging", "no"),
                StringMapEntryAdd("recycling:beverage_cartons", "no"),
                StringMapEntryAdd("recycling:plastic", "no")
            ),
            questType.answerApplied(RecyclingMaterials(listOf(PLASTIC_BOTTLES)))
        )
    }

    @Test fun `apply answer with PET`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:PET", "yes"),
                StringMapEntryAdd("recycling:plastic_bottles", "no"),
                StringMapEntryAdd("recycling:plastic_packaging", "no"),
                StringMapEntryAdd("recycling:beverage_cartons", "no"),
                StringMapEntryAdd("recycling:plastic", "no")
            ),
            questType.answerApplied(RecyclingMaterials(listOf(PET)))
        )
    }

    @Test fun `apply answer with plastic bottles and previous plastic answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:plastic_bottles", "yes"),
                StringMapEntryAdd("recycling:plastic_packaging", "no"),
                StringMapEntryAdd("recycling:beverage_cartons", "no"),
                StringMapEntryModify("recycling:plastic", "yes", "no")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC_BOTTLES)),
                mapOf("recycling:plastic" to "yes")
            )
        )
    }

    @Test fun `apply answer with plastic bottles and previous plastic packaging answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:plastic", "no", "no"),
                StringMapEntryAdd("recycling:plastic_bottles", "yes"),
                StringMapEntryAdd("recycling:beverage_cartons", "no"),
                StringMapEntryModify("recycling:plastic_packaging", "yes", "no")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC_BOTTLES)),
                mapOf(
                    "recycling:plastic" to "no",
                    "recycling:plastic_packaging" to "yes"
                )
            )
        )
    }

    @Test fun `apply answer with plastic packaging and previous plastic answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:plastic_packaging", "yes"),
                StringMapEntryModify("recycling:plastic", "yes", "no")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC_PACKAGING)),
                mapOf(
                    "recycling:plastic" to "yes"
                )
            )
        )
    }

    @Test fun `apply answer with plastic packaging and previous plastic bottles answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:plastic_packaging", "no", "yes"),
                StringMapEntryDelete("recycling:plastic_bottles", "yes"),
                StringMapEntryModify("recycling:plastic", "no", "no")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC_PACKAGING)),
                mapOf(
                    "recycling:plastic_bottles" to "yes",
                    "recycling:plastic_packaging" to "no",
                    "recycling:plastic" to "no"
                )
            )
        )
    }

    @Test fun `apply answer with plastic and previous plastic bottles answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:plastic", "no", "yes"),
                StringMapEntryDelete("recycling:plastic_bottles", "yes"),
                StringMapEntryDelete("recycling:plastic_packaging", "no")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC)),
                mapOf(
                    "recycling:plastic_bottles" to "yes",
                    "recycling:plastic_packaging" to "no",
                    "recycling:plastic" to "no"
                )
            )
        )
    }

    @Test fun `apply answer with plastic and previous plastic packaging answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:plastic", "no", "yes"),
                StringMapEntryDelete("recycling:plastic_packaging", "yes")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PLASTIC)),
                mapOf(
                    "recycling:plastic" to "no",
                    "recycling:plastic_packaging" to "yes"
                )
            )
        )
    }

    @Test fun `apply answer with same answers as before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:paper", "yes", "yes"),
                StringMapEntryModify("recycling:clothes", "yes", "yes"),
                StringMapEntryAdd("check_date:recycling", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(CLOTHES, PAPER)),
                mapOf(
                    "recycling:paper" to "yes",
                    "recycling:clothes" to "yes"
                )
            )
        )
    }

    @Test fun `apply answer removes previous yes-answers`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:shoes", "yes"),
                StringMapEntryModify("recycling:paper", "yes", "yes"),
                StringMapEntryDelete("recycling:cooking_oil", "yes"),
                StringMapEntryDelete("recycling:green_waste", "yes")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(SHOES, PAPER)),
                mapOf(
                    "recycling:paper" to "yes",
                    "recycling:cooking_oil" to "yes",
                    "recycling:green_waste" to "yes"
                )
            )
        )
    }

    @Test fun `apply answer updates previous check dates`() {
        assertEquals(
            setOf(
                StringMapEntryModify("recycling:paper", "no", "yes"),
                StringMapEntryDelete("recycling:check_date", "2000-11-01"),
                StringMapEntryModify("check_date:recycling", "2000-11-02", nowAsCheckDateString()),
                StringMapEntryDelete("recycling:lastcheck", "2000-11-03"),
                StringMapEntryDelete("lastcheck:recycling", "2000-11-04"),
                StringMapEntryDelete("recycling:last_checked", "2000-11-05"),
                StringMapEntryDelete("last_checked:recycling", "2000-11-06")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PAPER)),
                mapOf(
                    "recycling:paper" to "no",
                    "recycling:check_date" to "2000-11-01",
                    "check_date:recycling" to "2000-11-02",
                    "recycling:lastcheck" to "2000-11-03",
                    "lastcheck:recycling" to "2000-11-04",
                    "recycling:last_checked" to "2000-11-05",
                    "last_checked:recycling" to "2000-11-06"
                )
            )
        )
    }

    @Test fun `apply answer doesn't touch previous no-answers`() {
        assertEquals(
            setOf(StringMapEntryAdd("recycling:paper", "yes")),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PAPER)),
                mapOf(
                    "recycling:cooking_oil" to "no",
                    "recycling:green_waste" to "no"
                )
            )
        )
    }

    @Test fun `apply waste answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("amenity", "recycling", "waste_disposal"),
                StringMapEntryDelete("recycling_type", "container")
            ),
            questType.answerAppliedTo(IsWasteContainer, mapOf(
                "amenity" to "recycling",
                "recycling_type" to "container"
            ))
        )
    }

    @Test fun `apply waste answer deletes check dates for recycling and any previous recycling keys`() {
        assertEquals(
            setOf(
                StringMapEntryModify("amenity", "recycling", "waste_disposal"),
                StringMapEntryDelete("recycling_type", "container"),
                StringMapEntryDelete("check_date:recycling", "2000-11-11"),
                StringMapEntryDelete("recycling:check_date", "2000-11-11"),
                StringMapEntryDelete("recycling:lastcheck", "2000-11-11"),
                StringMapEntryDelete("lastcheck:recycling", "2000-11-11"),
                StringMapEntryDelete("recycling:last_checked", "2000-11-11"),
                StringMapEntryDelete("last_checked:recycling", "2000-11-11"),
                StringMapEntryDelete("recycling:something", "yes"),
                StringMapEntryDelete("recycling:another_thing", "no")
            ),
            questType.answerAppliedTo(
                IsWasteContainer,
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
                )
            )
        )
    }

    @Test fun `answer does not delete other =yes-keys`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("recycling:paper", "yes")
            ),
            questType.answerAppliedTo(
                RecyclingMaterials(listOf(PAPER)),
                mapOf("amenity" to "recycling", "access" to "yes")
            )
        )
    }
}
