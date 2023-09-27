package de.westnordost.streetcomplete.data

import android.database.sqlite.SQLiteOpenHelper
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

open class ApplicationDbTestCase {
    protected lateinit var dbHelper: SQLiteOpenHelper
    protected lateinit var database: Database

    @BeforeTest fun setUpHelper() {
        dbHelper = StreetCompleteSQLiteOpenHelper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DATABASE_NAME
        )
        database = AndroidDatabase(dbHelper)
    }

    @Test fun databaseAvailable() {
        assertNotNull(dbHelper.readableDatabase)
    }

    @AfterTest fun tearDownHelper() {
        dbHelper.close()
        InstrumentationRegistry.getInstrumentation().targetContext
            .deleteDatabase(DATABASE_NAME)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}
