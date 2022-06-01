package de.westnordost.streetcomplete.data

import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.streetcomplete.ApplicationConstants
import org.koin.dsl.module

val dbModule = module {
    single<Database> { AndroidDatabase(get()) }
    single<SQLiteOpenHelper> { StreetCompleteSQLiteOpenHelper(get(), ApplicationConstants.DATABASE_NAME) }
}
