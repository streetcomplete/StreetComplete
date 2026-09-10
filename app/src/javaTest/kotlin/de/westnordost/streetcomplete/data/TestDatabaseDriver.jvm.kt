package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

internal actual fun openTestDatabase(name: String): SQLiteConnection =
    BundledSQLiteDriver().open(name)
