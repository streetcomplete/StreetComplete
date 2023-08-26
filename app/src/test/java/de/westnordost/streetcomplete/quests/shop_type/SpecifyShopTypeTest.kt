package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpecifyShopTypeTest {
    private val questType = SpecifyShopType()

    @Test fun `is applicable to undefine shop`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("shop" to "yes"))
        ))
    }

    @Test fun `is not applicable when shop=yes is used as a property`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "fuel", "shop" to "yes"))
        ))
    }

    @Test fun `is not applicable when other primary tags are present`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("power" to "plant", "shop" to "yes"))
        ))
    }
}
