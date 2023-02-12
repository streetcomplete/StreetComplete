package de.westnordost.streetcomplete.quests.accepts_cards

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert
import org.junit.Test

class AddAcceptsCardsTest {
    private val questType = AddAcceptsCards()

    @Test
    fun `set stile as squeezer`() {
        questType.verifyAnswer(
            mapOf(
                "barrier" to "stile",
            ),
            CardAcceptance.DEBIT_AND_CREDIT,
            StringMapEntryAdd("payment:debit_cards", "yes"),
            StringMapEntryAdd("payment:credit_cards", "yes"),
        )
    }

    @Test
    fun `applicable to grocery shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags=mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }
}
