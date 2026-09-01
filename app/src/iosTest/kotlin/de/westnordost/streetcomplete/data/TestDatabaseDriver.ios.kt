package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.NativeSQLiteDriver

internal actual fun openTestDatabase(name: String): SQLiteConnection =
    NativeSQLiteDriver().open(name)
