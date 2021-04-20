package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test
import java.time.LocalDate

class CheckShopTypeTest {
    private val questType = CheckShopType()

    @Test fun `apply shop vacant answer`() {
        questType.verifyAnswer(
            IsShopVacant,
            StringMapEntryAdd("check_date", LocalDate.now().toCheckDateString())
        )
    }

    @Test fun `apply shop vacant answer when check date is already set`() {
        questType.verifyAnswer(
            mapOf("check_date" to "already set"),
            IsShopVacant,
            StringMapEntryModify("check_date", "already set", LocalDate.now().toCheckDateString())
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
            StringMapEntryAdd("check_date", LocalDate.now().toCheckDateString()),
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

    @Test fun `apply shop with tags answer removes disused-shop`() {
        questType.verifyAnswer(
            mapOf("disused:shop" to "yes"),
            ShopType(mapOf("a" to "b")),
            StringMapEntryAdd("a", "b"),
            StringMapEntryDelete("disused:shop", "yes")
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
}
