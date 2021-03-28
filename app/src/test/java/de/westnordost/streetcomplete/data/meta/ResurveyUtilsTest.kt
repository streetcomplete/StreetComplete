package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.Calendar.MILLISECOND


class ResurveyUtilsTest {
    @Test fun toCheckDateString() {
        val cal = Calendar.getInstance()
        cal.set(2007, 11, 8)
        assertEquals("2007-12-08", cal.time.toCheckDateString())
    }

    @Test fun fromCheckDateString() {
        val cal = Calendar.getInstance()
        cal.set(2007, 11, 8, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        assertEquals(cal.time, "2007-12-08".toCheckDate())
    }

    @Test fun `updateWithCheckDate adds new tag`() {
        val builder = StringMapChangesBuilder(emptyMap())
        builder.updateWithCheckDate("new key", "tag")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryAdd("new key", "tag")
        ), changes)
    }

    @Test fun `updateWithCheckDate modifies tag`() {
        val builder = StringMapChangesBuilder(mapOf("old key" to "old value"))
        builder.updateWithCheckDate("old key", "new value")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryModify("old key", "old value", "new value")
        ), changes)
    }

    @Test fun `updateWithCheckDate adds check date`() {
        val builder = StringMapChangesBuilder(mapOf("key" to "value"))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryAdd("check_date:key", Date().toCheckDateString())
        ), changes)
    }

    @Test fun `updateWithCheckDate modifies check date`() {
        val builder = StringMapChangesBuilder(mapOf("key" to "value", "check_date:key" to "2000-11-11"))
        builder.updateWithCheckDate("key", "value")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryModify("check_date:key", "2000-11-11", Date().toCheckDateString())
        ), changes)
    }

    @Test fun `updateWithCheckDate removes old check dates on modifying key`() {
        val builder = StringMapChangesBuilder(mapOf(
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
            StringMapEntryDelete("key:check_date", "2000-11-01"),
            StringMapEntryDelete("check_date:key", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateWithCheckDate removes old check dates on adding key`() {
        val builder = StringMapChangesBuilder(mapOf(
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
            StringMapEntryDelete("check_date:key", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateWithCheckDate removes old check dates on modifying check date`() {
        val builder = StringMapChangesBuilder(mapOf(
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
            StringMapEntryModify("check_date:key", "2000-11-01", Date().toCheckDateString()),
            StringMapEntryDelete("key:check_date", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }

    @Test fun `updateCheckDateForKey adds check date`() {
        val builder = StringMapChangesBuilder(mapOf())
        builder.updateCheckDateForKey("key")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryAdd("check_date:key", Date().toCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDateForKey modifies check date`() {
        val builder = StringMapChangesBuilder(mapOf("check_date:key" to "2000-11-11"))
        builder.updateCheckDateForKey("key")
        val changes = builder.create().changes

        assertEquals(listOf(
            StringMapEntryModify("check_date:key", "2000-11-11", Date().toCheckDateString())
        ), changes)
    }

    @Test fun `updateCheckDateForKey removes old check dates on modifying check date`() {
        val builder = StringMapChangesBuilder(mapOf(
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
            StringMapEntryModify("check_date:key", "2000-11-01", Date().toCheckDateString()),
            StringMapEntryDelete("key:check_date", "2000-11-02"),
            StringMapEntryDelete("key:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:key", "2000-11-04"),
            StringMapEntryDelete("key:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:key", "2000-11-06")
        )))
    }
}