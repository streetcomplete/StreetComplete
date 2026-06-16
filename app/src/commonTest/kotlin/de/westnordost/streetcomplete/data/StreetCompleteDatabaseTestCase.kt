package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class StreetCompleteDatabaseTestCase {
    protected lateinit var database: Database
    private lateinit var connection: SQLiteConnection

    @BeforeTest fun setUp() {
        SystemFileSystem.delete(Path(DATABASE_NAME), mustExist = false)
        connection = BundledSQLiteDriver().open(DATABASE_NAME)
        database = DatabaseImpl(connection)
        database.initialize(StreetCompleteDatabaseConfigurator)
    }

    @AfterTest fun tearDown() {
        connection.close()
        SystemFileSystem.delete(Path(DATABASE_NAME), mustExist = false)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}
