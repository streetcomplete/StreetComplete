package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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

    @Test fun `is not applicable to disused shop that is not disused after all, apparently`() {
        assertFalse(questType.isApplicableTo(
            node(
                tags = mapOf("disused:shop" to "yes", "shop" to "something new already"),
                timestamp = 100
            )
        ))
    }

    @Test fun `apply shop vacant answer`() {
        questType.verifyAnswer(
            IsShopVacant,
            StringMapEntryAdd("check_date", nowAsCheckDateString())
        )
    }

    @Test fun `apply shop vacant answer when check date is already set`() {
        questType.verifyAnswer(
            mapOf("check_date" to "already set"),
            IsShopVacant,
            StringMapEntryModify("check_date", "already set", nowAsCheckDateString())
        )
    }

    @Test fun `apply shop vacant answer when other survey key is already set`() {
        questType.verifyAnswer(
            mapOf(
                "lastcheck" to "a",
                "last_checked" to "b",
                "survey:date" to "c",
                "survey_date" to "d"
            ),
            IsShopVacant,
            StringMapEntryAdd("check_date", nowAsCheckDateString()),
            StringMapEntryDelete("lastcheck", "a"),
            StringMapEntryDelete("last_checked", "b"),
            StringMapEntryDelete("survey:date", "c"),
            StringMapEntryDelete("survey_date", "d"),
        )
    }

    @Test fun `apply shop with tags answer`() {
        questType.verifyAnswer(
            ShopType(mapOf("a" to "b", "c" to "d")),
            StringMapEntryAdd("a", "b"),
            StringMapEntryAdd("c", "d"),
        )
    }

    @Test fun `apply shop with tags answer removes all previous survey keys`() {
        questType.verifyAnswer(
            mapOf(
                "check_date" to "1",
                "lastcheck" to "a",
                "last_checked" to "b",
                "survey:date" to "c",
                "survey_date" to "d"
            ),
            ShopType(mapOf("a" to "b")),
            StringMapEntryAdd("a", "b"),
            StringMapEntryDelete("check_date", "1"),
            StringMapEntryDelete("lastcheck", "a"),
            StringMapEntryDelete("last_checked", "b"),
            StringMapEntryDelete("survey:date", "c"),
            StringMapEntryDelete("survey_date", "d"),
        )
    }

    @Test fun `apply shop with tags answer removes disused-shop and amenity`() {
        questType.verifyAnswer(
            mapOf("disused:shop" to "yes", "disused:amenity" to "yes"),
            ShopType(mapOf("a" to "b")),
            StringMapEntryAdd("a", "b"),
            StringMapEntryDelete("disused:shop", "yes"),
            StringMapEntryDelete("disused:amenity", "yes"),
        )
    }

    @Test fun `apply shop with tags answer removes vacant shop tag`() {
        questType.verifyAnswer(
            mapOf("shop" to "vacant"),
            ShopType(mapOf("a" to "b")),
            StringMapEntryAdd("a", "b"),
            StringMapEntryDelete("shop", "vacant")
        )
    }

    @Test fun `apply shop with tags answer overwrites vacant shop`() {
        questType.verifyAnswer(
            mapOf("shop" to "vacant"),
            ShopType(mapOf("shop" to "not vacant")),
            StringMapEntryModify("shop", "vacant", "not vacant")
        )
    }

    // see KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED
    @Test fun `apply shop with tags answer clears only specified known safe-to-remove tags`() {
        questType.verifyAnswer(
            mapOf(
                "building" to "yes", // <- should not be cleared
                "disused:amenity" to "yes",
                "phone" to "123456",
                "ref" to "1111",
                "fee" to "yes",
                "nycdoitt:bin" to "22222",
                "barrier" to "wall",
                "office" to "it",
                "tourism" to "information",
                "information" to "office",
                "name" to "Juppiebude"
            ),
            ShopType(mapOf("shop" to "ice_cream")),
            StringMapEntryAdd("shop", "ice_cream"),
            StringMapEntryDelete("disused:amenity", "yes"),
            StringMapEntryDelete("phone", "123456"),
            StringMapEntryDelete("name", "Juppiebude"),
            StringMapEntryDelete("ref", "1111"),
            StringMapEntryDelete("fee", "yes"),
            StringMapEntryDelete("office", "it"),
            StringMapEntryDelete("tourism", "information"),
            StringMapEntryDelete("information", "office"),
        )
    }
}
