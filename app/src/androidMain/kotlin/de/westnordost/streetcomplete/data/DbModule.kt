package de.westnordost.streetcomplete.data

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.westnordost.streetcomplete.ApplicationConstants
import org.koin.dsl.module

val dbModule = module {
    single<Database> {
        val databaseFilePath = get<Context>().getDatabasePath(ApplicationConstants.DATABASE_NAME).path
        val databaseConnection = BundledSQLiteDriver().open(databaseFilePath)
        StreetCompleteDatabase(databaseConnection)
    }
}
