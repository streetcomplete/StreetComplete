package de.westnordost.streetcomplete.data

import android.database.sqlite.SQLiteOpenHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

open class ApplicationDbTestCase {
    protected lateinit var dbHelper: SQLiteOpenHelper
    protected lateinit var database: Database

    @Before fun setUpHelper() {
        dbHelper = StreetCompleteSQLiteOpenHelper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DATABASE_NAME
        )
        database = AndroidDatabase(dbHelper)
    }

    @Test fun databaseAvailable() {
        Assert.assertNotNull(dbHelper.readableDatabase)
    }

    @After fun tearDownHelper() {
        dbHelper.close()
        InstrumentationRegistry.getInstrumentation().targetContext
            .deleteDatabase(DATABASE_NAME)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}
