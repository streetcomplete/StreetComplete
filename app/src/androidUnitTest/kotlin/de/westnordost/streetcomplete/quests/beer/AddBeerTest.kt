package de.westnordost.streetcomplete.quests.beer

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddBeerTest {
    private val questType = AddBeer()

    @Test fun `not applicable to empty tags`() {
        assertFalse(questType.isApplicableTo(node()))
    }

    @Test fun `applicable to pub without beer tag`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub"))
        ))
    }

    @Test fun `not applicable to microbrewery`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub", "microbrewery" to "yes"))
        ))
    }

    @Test fun `not applicable to brewery`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub", "brewery" to "yes"))
        ))
    }

    @Test fun `applicable to pub with drink beer yes`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub", "drink:beer" to "yes"))
        ))
    }

    @Test fun `applicable to pub with drink beer served`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub", "drink:beer" to "served"))
        ))
    }

    @Test fun `not applicable to pub with drink beer draught`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "pub", "drink:beer" to "draught"))
        ))
    }

    @Test fun `applicable to restaurant`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "restaurant"))
        ))
    }

    @Test fun `draught answer sets correct answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("drink:beer", "draught")),
            questType.answerApplied(BeerServed.DRAUGHT)
        )
    }

    @Test fun `no answer sets correct answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("drink:beer", "no")),
            questType.answerApplied(BeerServed.NO)
        )
    }
}