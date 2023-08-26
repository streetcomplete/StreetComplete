package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class SQLiteDatabaseKtTest : ApplicationDbTestCase() {

    @BeforeTest fun setUp() {
        dbHelper.writableDatabase.execSQL("CREATE TABLE t (a int, b int)")
    }

    @AfterTest fun tearDown() {
        dbHelper.writableDatabase.execSQL("DROP TABLE t")
    }
}
