package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

open class ApplicationDbTestCase {
    protected lateinit var database: Database
    private lateinit var dbConnection: SQLiteConnection
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @BeforeTest fun setUpHelper() {
        val dbPath = context.getDatabasePath(DATABASE_NAME).path
        dbConnection = BundledSQLiteDriver().open(dbPath)
        database = StreetCompleteDatabase(dbConnection)
    }

    @Test fun databaseAvailable() {
        assertNotNull(database)
    }

    @AfterTest fun tearDownHelper() {
        dbConnection.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}
