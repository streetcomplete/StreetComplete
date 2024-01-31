package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckShopTypeTest {
    private val questType = CheckShopType()

    @Test fun `is applicable to vacant shop`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("shop" to "vacant"), timestamp = 100)
        ))
    }

    @Test fun `is applicable to disused shop`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("disused:shop" to "yes"), timestamp = 100)
        ))
    }

    @Test fun `is applicable to disused specific amenity`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("disused:amenity" to "bar"), timestamp = 100)
        ))
    }

    @Test fun `is not applicable to other disused amenity`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("disused:amenity" to "telephone"), timestamp = 100)
        ))
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("disused:amenity" to "yes"), timestamp = 100)
        ))
    }

    // for disused non-disused combo validator see https://josm.openstreetmap.de/ticket/22668#ticket
    @Test fun `is not applicable to disused shop that is not disused after all, apparently`() {
        assertFalse(questType.isApplicableTo(
            node(
                tags = mapOf("disused:shop" to "yes", "shop" to "something new already"),
                timestamp = 100
            )
        ))
    }

    @Test fun `is not applicable to disused shop that is not disused after all, with well specified alternative`() {
        assertFalse(questType.isApplicableTo(
            node(
                tags = mapOf("disused:shop" to "yes", "shop" to "mall"),
                timestamp = 100
            )
        ))
    }

    @Test fun `is not applicable to disused shop that is not disused after all, with well specified non-shop alternative`() {
        assertFalse(questType.isApplicableTo(
            node(
                tags = mapOf("disused:shop" to "yes", "amenity" to "clothing_bank"),
                timestamp = 100
            )
        ))
    }

    @Test fun `apply shop vacant answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("check_date", nowAsCheckDateString())),
            questType.answerApplied(IsShopVacant)
        )
    }

    @Test fun `apply shop with tags answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("a", "b"),
                StringMapEntryAdd("c", "d")
            ),
            questType.answerApplied(ShopType(mapOf("a" to "b", "c" to "d")))
        )
    }
}
