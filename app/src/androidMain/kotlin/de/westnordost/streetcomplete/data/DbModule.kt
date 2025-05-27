package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.ApplicationConstants
import org.koin.dsl.module

val dbModule = module {
    single<Database> {
        val sqLite = StreetCompleteSQLiteOpenHelper(get(), ApplicationConstants.DATABASE_NAME)
        AndroidDatabase(sqLite.writableDatabase)
    }
}
