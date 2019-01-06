package de.westnordost.streetcomplete.ktx

import android.database.sqlite.SQLiteDatabase

/**
 * Run [body] in a transaction marking it as successful if it completes without exception.
 */
inline fun <T> SQLiteDatabase.transaction(body: SQLiteDatabase.() -> T): T {
    beginTransaction()
    try {
        val result = body()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}
