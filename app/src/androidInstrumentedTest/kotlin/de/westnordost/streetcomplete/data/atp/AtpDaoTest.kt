package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AtpDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: AtpDao

    @BeforeTest fun createDao() {
        dao = AtpDao(database)
    }

    @Test fun putGet() {
        val entry = createAtpEntry()

        dao.put(entry)
        val dbAtp = dao.get(entry.id)!!
        assertEquals(entry, dbAtp)
    }

    @Test fun putAll() {
        dao.putAll(listOf(createAtpEntry(1), createAtpEntry(2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }


    @Test fun deleteButNothingIsThere() {
        assertFalse(dao.delete(1))
    }

    @Test fun delete() {
        val entry = createAtpEntry()
        dao.put(entry)
        assertTrue(dao.delete(entry.id))
        assertNull(dao.get(entry.id))
        assertFalse(dao.delete(entry.id))
    }

    @Test fun getAllPositions() {
        val thisIsIn = createAtpEntry(1, LatLon(0.5, 0.5))
        val thisIsOut = createAtpEntry(2, LatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(LatLon(0.5, 0.5), positions.single())
    }

    @Test fun getAllByBbox() {
        val thisIsIn = createAtpEntry(1, LatLon(0.5, 0.5))
        val thisIsOut = createAtpEntry(2, LatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val entries = dao.getAll(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(thisIsIn, entries.single())
    }

    @Test fun getAllByIds() {
        val first = createAtpEntry(1)
        val second = createAtpEntry(2)
        val third = createAtpEntry(3)
        dao.putAll(listOf(first, second, third))

        assertEquals(listOf(first, second), dao.getAll(listOf(1, 2)))
    }

    @Test fun getAllWithMatchingOsmElement() {
        val first = createAtpEntry(1, osmMatch = ElementKey(NODE, 1))
        val second = createAtpEntry(2, osmMatch = ElementKey(RELATION, 1))
        val third = createAtpEntry(3, osmMatch = ElementKey(WAY, 11))
        dao.putAll(listOf(first, second, third))

        assertEquals(listOf(first), dao.getAllWithMatchingOsmElement(ElementKey(NODE, 1)))
        assertEquals(listOf(), dao.getAllWithMatchingOsmElement(ElementKey(NODE, 2)))
        assertEquals(listOf(third), dao.getAllWithMatchingOsmElement(ElementKey(WAY, 11)))
    }

    @Test fun deleteAllByIds() {
        dao.putAll(listOf(createAtpEntry(1), createAtpEntry(2), createAtpEntry(3)))

        assertEquals(2, dao.deleteAll(listOf(1, 2)))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
        assertNotNull(dao.get(3))
    }

    @Test fun getUnusedAndOldIds() {
        dao.putAll(listOf(createAtpEntry(1), createAtpEntry(2), createAtpEntry(3)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(1L, 2L, 3L)))
    }

    @Test fun getUnusedAndOldIdsButAtMostX() {
        dao.putAll(listOf(createAtpEntry(1), createAtpEntry(2), createAtpEntry(3)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10, 2)
        assertEquals(2, unusedIds.size)
    }

    @Test fun clear() {
        dao.putAll(listOf(createAtpEntry(1), createAtpEntry(2), createAtpEntry(3)))
        dao.clear()
        assertTrue(dao.getAll(listOf(1L, 2L, 3L)).isEmpty())
    }
}

private fun createAtpEntry(
    id: Long = 5,
    position: LatLon = LatLon(1.0, 1.0),
    osmMatch: ElementKey = ElementKey(NODE, 1),
    tagsInATP: Map<String, String> = mapOf(),
    tagsInOSM: Map<String, String> = mapOf(),
    reportType: ReportType = ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
) = AtpEntry(
        position = position,
        id = id,
        osmMatch = osmMatch,
        tagsInATP = tagsInATP,
        tagsInOSM = tagsInOSM,
        reportType = reportType,
    )
