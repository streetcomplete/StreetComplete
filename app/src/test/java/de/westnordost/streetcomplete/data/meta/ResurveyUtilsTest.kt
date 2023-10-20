package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.toCheckDate
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResurveyUtilsTest {
    @Test fun toCheckDateString() {
        assertEquals("2007-12-08", LocalDate(2007, 12, 8).toCheckDateString())
    }

    @Test fun fromCheckDateString() {
        assertEquals(LocalDate(2007, 12, 8), "2007-12-08".toCheckDate())
    }

    @Test fun `updateWithCheckDate adds new tag`() {
        val builder = Tags(emptyMap())
        builder.updateWithCheckDate("new key", "tag")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryAdd("new key", "tag")
        ), changes)
    }

    @Test fun hasCheckDateForKey() {
        assertFalse(Tags(mapOf("key" to "value")).hasCheckDateForKey("key"))

        assertFalse(Tags(mapOf(
            "key" to "value",
            "check_date:another_key" to "value"
        )).hasCheckDateForKey("key"))

        assertTrue(Tags(mapOf(
            "key" to "value",
            "check_date:another_key" to "value"
        )).hasCheckDateForKey("another_key"))
    }

    @Test fun `updateWithCheckDate modifies tag`() {
        val builder = Tags(mapOf("old key" to "old value"))
        builder.updateWithCheckDate("old key", "new value")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryModify("old key", "old value", "new value")
        ), changes)
    }

    @Test fun `updateWithCheckDate adds check date`() {
        val builder = Tags(mapOf("key" to "value"))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryModify("key", "value", "value"),
            StringMapEntryAdd("check_date:key", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateWithCheckDate modifies check date`() {
        val builder = Tags(mapOf("key" to "value", "check_date:key" to "2000-11-11"))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryModify("key", "value", "value"),
            StringMapEntryModify("check_date:key", "2000-11-11", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateWithCheckDate modifies old check date on modifying key`() {
        val builder = Tags(mapOf(
            "key" to "old value",
            "key:check_date" to "2000-11-01",
            "check_date:key" to "2000-11-02",
            "key:lastcheck" to "2000-11-03",
            "lastcheck:key" to "2000-11-04",
            "key:last_checked" to "2000-11-05",
            "last_checked:key" to "2000-11-06"
        ))
        builder.updateWithCheckDate("key", "new value")
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryModify("key", "old value", "new value"),
            StringMapEntryModify("check_date:key", "2000-11-02", nowAsCheckDateString()),
            StringMapEntryDelete("key:check_date", "2000-11-01"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateWithCheckDate modifies old check dates on adding key`() {
        val builder = Tags(mapOf(
            "key:check_date" to "2000-11-01",
            "check_date:key" to "2000-11-02",
            "key:lastcheck" to "2000-11-03",
            "lastcheck:key" to "2000-11-04",
            "key:last_checked" to "2000-11-05",
            "last_checked:key" to "2000-11-06"
        ))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryAdd("key", "value"),
            StringMapEntryDelete("key:check_date", "2000-11-01"),
            StringMapEntryModify("check_date:key", "2000-11-02", nowAsCheckDateString()),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateWithCheckDate removes old check dates on modifying check date`() {
        val builder = Tags(mapOf(
            "key" to "value",
            "check_date:key" to "2000-11-01",
            "key:check_date" to "2000-11-02",
            "key:lastcheck" to "2000-11-03",
            "lastcheck:key" to "2000-11-04",
            "key:last_checked" to "2000-11-05",
            "last_checked:key" to "2000-11-06"
        ))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryModify("key", "value", "value"),
            StringMapEntryModify("check_date:key", "2000-11-01", nowAsCheckDateString()),
            StringMapEntryDelete("key:check_date", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateCheckDateForKey adds check date`() {
        val builder = Tags(mapOf())
        builder.updateCheckDateForKey("key")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryAdd("check_date:key", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDateForKey modifies check date`() {
        val builder = Tags(mapOf("check_date:key" to "2000-11-11"))
        builder.updateCheckDateForKey("key")
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryModify("check_date:key", "2000-11-11", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDateForKey removes old check dates on modifying check date`() {
        val builder = Tags(mapOf(
            "check_date:key" to "2000-11-01",
            "key:check_date" to "2000-11-02",
            "key:lastcheck" to "2000-11-03",
            "lastcheck:key" to "2000-11-04",
            "key:last_checked" to "2000-11-05",
            "last_checked:key" to "2000-11-06"
        ))
        builder.updateCheckDateForKey("key")
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryModify("check_date:key", "2000-11-01", nowAsCheckDateString()),
            StringMapEntryDelete("key:check_date", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateCheckDate adds check date`() {
        val builder = Tags(mapOf())
        builder.updateCheckDate()
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryAdd("check_date", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDate modifies check date`() {
        val builder = Tags(mapOf("check_date" to "2000-11-11"))
        builder.updateCheckDate()
        val changes = builder.create().changes

        assertEquals(setOf(
            StringMapEntryModify("check_date", "2000-11-11", nowAsCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDate removes old check dates on modifying check date`() {
        val builder = Tags(mapOf(
            "check_date" to "2000-11-01",
            "lastcheck" to "2000-11-02",
            "last_checked" to "2000-11-03",
        ))
        builder.updateCheckDate()
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryModify("check_date", "2000-11-01", nowAsCheckDateString()),
            StringMapEntryDelete("lastcheck", "2000-11-02"),
            StringMapEntryDelete("last_checked", "2000-11-03"),
        )))
    }

    @Test fun `removeCheckDates does not add a check date`() {
        val builder = Tags(mapOf())
        builder.removeCheckDates()
        val changes = builder.create().changes

        assertTrue(changes.isEmpty())
    }

    @Test fun `removeCheckDates removes check date`() {
        val builder = Tags(mapOf("check_date" to "2000-11-11"))
        builder.removeCheckDates()
        val changes = builder.create().changes

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryDelete("check_date", "2000-11-11"),
        )))
    }

    @Test fun `removeCheckDates removes all check dates`() {
        val builder = Tags(mapOf(
            "check_date" to "2000-11-01",
            "lastcheck" to "2000-11-02",
            "last_checked" to "2000-11-03",
        ))
        builder.removeCheckDates()
        val changes = builder.create().changes.toSet()

        assertTrue(changes.containsExactlyInAnyOrder(listOf(
            StringMapEntryDelete("check_date", "2000-11-01"),
            StringMapEntryDelete("lastcheck", "2000-11-02"),
            StringMapEntryDelete("last_checked", "2000-11-03"),
        )))
    }
}
