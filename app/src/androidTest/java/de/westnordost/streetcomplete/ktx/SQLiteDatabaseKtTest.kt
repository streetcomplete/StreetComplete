package de.westnordost.streetcomplete.ktx

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SQLiteDatabaseKtTest : ApplicationDbTestCase() {

    @Before fun setUp() {
        dbHelper.writableDatabase.execSQL("CREATE TABLE t (a int, b int)")
    }

    @After fun tearDown() {
        dbHelper.writableDatabase.execSQL("DROP TABLE t")
    }

    @Test fun hasColumn() {
        assertFalse(dbHelper.writableDatabase.hasColumn("t", "c"))
        assertTrue(dbHelper.writableDatabase.hasColumn("t", "a"))
    }
}
