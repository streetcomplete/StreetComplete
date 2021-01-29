package de.westnordost.streetcomplete.data.osm.mapdata

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import org.junit.After
import org.junit.Before
import org.junit.Test

import de.westnordost.osmapi.map.data.Element

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.mockito.Mockito.*
import java.util.*

class AOsmElementDaoTest {

    private lateinit var dao: TestOsmElementDao
    private lateinit var dbHelper: SQLiteOpenHelper

    @Before fun setUpHelper() {
        dbHelper = TestDbHelper(getInstrumentation().targetContext)
        dao = TestOsmElementDao(dbHelper)
    }

    @After fun tearDownHelper() {
        dbHelper.close()
        getInstrumentation().targetContext.deleteDatabase(TESTDB)
    }

    @Test fun putGet() {
        dao.put(createElement(6, 1))
        assertEquals(6, dao.get(6)!!.id)
        assertEquals(1, dao.get(6)!!.version)
    }

    @Test fun putAll() {
        dao.putAll(listOf(createElement(1, 2), createElement(2, 2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun putOverwrite() {
        dao.put(createElement(6, 0))
        dao.put(createElement(6, 5))
        assertEquals(5, dao.get(6)!!.version)
    }

    @Test fun deleteAll() {
        dao.putAll(listOf(
            createElement(1, 2),
            createElement(2, 2),
            createElement(3, 2)
        ))
        dao.deleteAll(listOf(1,2,4))
        assertNotNull(dao.get(3))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
    }

    @Test fun getAll() {
        dao.putAll(listOf(
            createElement(1, 2),
            createElement(2, 2),
            createElement(3, 2)
        ))
        assertTrue(dao.getAll(listOf(1,2,4)).containsExactlyInAnyOrder(listOf(1, 2)))
    }

    @Test fun getNull() {
        assertNull(dao.get(6))
    }

    @Test fun delete() {
        dao.put(createElement(6, 0))
        dao.delete(6)
        assertNull(dao.get(6))
    }

    @Test fun getUnusedAndOldIds() {
        val db = dbHelper.writableDatabase
        db.insert(OsmQuestTable.NAME, null, contentValuesOf(
            OsmQuestTable.Columns.ELEMENT_ID to 1L,
            OsmQuestTable.Columns.ELEMENT_TYPE to "NODE"
        ))
        db.insert(UndoOsmQuestTable.NAME, null, contentValuesOf(
            UndoOsmQuestTable.Columns.ELEMENT_ID to 2L,
            UndoOsmQuestTable.Columns.ELEMENT_TYPE to "NODE"
        ))
        db.insert(DeleteOsmElementTable.NAME, null, contentValuesOf(
            DeleteOsmElementTable.Columns.ELEMENT_ID to 3L,
            DeleteOsmElementTable.Columns.ELEMENT_TYPE to "NODE"
        ))
        dao.putAll(listOf(
            createElement(1L, 1),
            createElement(2L, 1),
            createElement(3L, 1),
            createElement(4L, 1),
        ))
        val unusedIds = dao.getUnusedAndOldIds(System.currentTimeMillis() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(4L)))
    }
}

private fun createElement(id: Long, version: Int): Element {
    val element = mock(Element::class.java)
    `when`(element.id).thenReturn(id)
    `when`(element.type).thenReturn(Element.Type.NODE)
    `when`(element.version).thenReturn(version)
    return element
}

private const val TABLE_NAME = "test"
private const val ID_COL = "id"
private const val LAST_UPDATE_COL = "last_update"
private const val VERSION_COL = "version"

private const val TESTDB = "testdb.db"

private class TestOsmElementDao(dbHelper: SQLiteOpenHelper) : AOsmElementDao<Element>(dbHelper) {

    override val elementTypeName = Element.Type.NODE.name
    override val tableName = TABLE_NAME
    override val idColumnName = ID_COL
    override val lastUpdateColumnName = LAST_UPDATE_COL
    override val mapping = object : ObjectRelationalMapping<Element> {
        override fun toContentValues(obj: Element) = contentValuesOf(
            ID_COL to obj.id,
            VERSION_COL to obj.version,
            LAST_UPDATE_COL to Date().time
        )

        override fun toObject(cursor: Cursor) = createElement(cursor.getLong(0), cursor.getInt(1))
    }
}

private class TestDbHelper(context: Context) : SQLiteOpenHelper(context, TESTDB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE ${OsmQuestTable.NAME} (
                ${OsmQuestTable.Columns.ELEMENT_ID} int NOT NULL,
                ${OsmQuestTable.Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE ${UndoOsmQuestTable.NAME} (
                ${UndoOsmQuestTable.Columns.ELEMENT_ID} int NOT NULL,
                ${UndoOsmQuestTable.Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE ${DeleteOsmElementTable.NAME} (
                ${DeleteOsmElementTable.Columns.ELEMENT_ID} int NOT NULL,
                ${DeleteOsmElementTable.Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_NAME (
                $ID_COL int PRIMARY KEY,
                $VERSION_COL int NOT NULL,
                $LAST_UPDATE_COL int NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
