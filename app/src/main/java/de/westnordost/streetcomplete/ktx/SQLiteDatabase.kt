package de.westnordost.streetcomplete.ktx

import android.database.Cursor
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


fun SQLiteDatabase.query(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null
) = query(table, columns, selection, selectionArgs, null, null, null, null)

fun SQLiteDatabase.queryOne(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null
) = query(table, columns, selection, selectionArgs, null, null, null, "1")
