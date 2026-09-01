package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class StreetCompleteDatabaseTestCase {
    private lateinit var database: Database
    private lateinit var connection: SQLiteConnection

    @BeforeTest
    open fun setUpDatabase() {
        SystemFileSystem.delete(Path(DATABASE_NAME), mustExist = false)
        connection = openTestDatabase(DATABASE_NAME)
        database = DatabaseImpl(connection)
        database.initialize(StreetCompleteDatabaseConfigurator)
        onDatabaseInitialized(database)
    }

    abstract fun onDatabaseInitialized(database: Database)

    @AfterTest fun tearDown() {
        connection.close()
        SystemFileSystem.delete(Path(DATABASE_NAME), mustExist = false)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}

internal expect fun openTestDatabase(name: String): SQLiteConnection
