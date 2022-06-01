package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.After
import org.junit.Before

class SQLiteDatabaseKtTest : ApplicationDbTestCase() {

    @Before fun setUp() {
        dbHelper.writableDatabase.execSQL("CREATE TABLE t (a int, b int)")
    }

    @After fun tearDown() {
        dbHelper.writableDatabase.execSQL("DROP TABLE t")
    }
}
