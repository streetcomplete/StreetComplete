package de.westnordost.streetcomplete.data.osm.persist

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
import org.junit.Assert.*
import org.mockito.Mockito.*

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

    @Test fun getNull() {
        assertNull(dao.get(6))
    }

    @Test fun delete() {
        dao.put(createElement(6, 0))
        dao.delete(6)
        assertNull(dao.get(6))
    }
}

private fun createElement(id: Long, version: Int): Element {
    val element = mock(Element::class.java)
    `when`(element.id).thenReturn(id)
    `when`(element.version).thenReturn(version)
    return element
}

private const val TABLE_NAME = "test"
private const val ID_COL = "id"
private const val VERSION_COL = "version"

private const val TESTDB = "testdb.db"

private class TestOsmElementDao(dbHelper: SQLiteOpenHelper) : AOsmElementDao<Element>(dbHelper) {

    override val elementTypeName = Element.Type.NODE.name
    override val tableName = TABLE_NAME
    override val idColumnName = ID_COL
    override val mapping = object : ObjectRelationalMapping<Element> {
        override fun toContentValues(obj: Element) = contentValuesOf(
            ID_COL to obj.id,
            VERSION_COL to obj.version
        )

        override fun toObject(cursor: Cursor) = createElement(cursor.getLong(0), cursor.getInt(1))
    }
}

private class TestDbHelper(context: Context) : SQLiteOpenHelper(context, TESTDB, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // the AOsmElementDao is tied to the quest table... but we only need the id and type
        db.execSQL(
            "CREATE TABLE " + OsmQuestTable.NAME + " (" +
                OsmQuestTable.Columns.ELEMENT_ID + " int            NOT NULL, " +
                OsmQuestTable.Columns.ELEMENT_TYPE + " varchar(255)    NOT NULL " +
                ");"
        )
        db.execSQL(
            "INSERT INTO " + OsmQuestTable.NAME + " (" +
                OsmQuestTable.Columns.ELEMENT_ID + ", " +
                OsmQuestTable.Columns.ELEMENT_TYPE + ") VALUES " +
                "(1, \"" + Element.Type.NODE.name + "\");"
        )

        db.execSQL(
            "CREATE TABLE " + TABLE_NAME + " ( " +
                ID_COL + " int PRIMARY KEY, " +
                VERSION_COL + " int);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}
