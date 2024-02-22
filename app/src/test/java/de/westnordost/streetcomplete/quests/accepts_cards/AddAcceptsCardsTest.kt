package de.westnordost.streetcomplete.quests.accepts_cards

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class AddAcceptsCardsTest {
    private val questType = AddAcceptsCards()

    @Test fun `sets expected tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("payment:debit_cards", "yes"),
                StringMapEntryAdd("payment:credit_cards", "yes"),
            ),
            questType.answerApplied(CardAcceptance.DEBIT_AND_CREDIT)
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("payment:debit_cards", "yes"),
                StringMapEntryAdd("payment:credit_cards", "no"),
            ),
            questType.answerApplied(CardAcceptance.DEBIT_CARDS_ONLY)
        )
    }

    @Test fun `applicable to greengrocer shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to weird shop values`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("shop" to "dgfygyug", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to vacant shop`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("shop" to "vacant", "name" to "Foobar")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
