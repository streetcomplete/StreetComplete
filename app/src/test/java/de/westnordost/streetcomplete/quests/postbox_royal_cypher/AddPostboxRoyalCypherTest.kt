package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPostboxRoyalCypherTest {

    private val questType = AddPostboxRoyalCypher()

    @Test fun `not applicable to postboxes that already have a royal cypher mapped`() {
        assertEquals(false, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box", "royal_cypher" to "EIIR"))
        ))
    }

    @Test fun `applicable to postboxes that not already have a royal cypher mapped`() {
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"))
        ))
    }

    @Test fun `apply no cypher answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("royal_cypher", "no")),
            questType.answerApplied(PostboxRoyalCypher.NONE)
        )
    }

    @Test fun `apply Charles III answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("royal_cypher", "CIIIR")),
            questType.answerApplied(PostboxRoyalCypher.CHARLES_III)
        )
    }
}
