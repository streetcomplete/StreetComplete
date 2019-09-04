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


fun <R> SQLiteDatabase.query(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    transform: (Cursor) -> R
): List<R> = query(table, columns, selection, selectionArgs, null, null, null, null).use { cursor ->
    val result = mutableListOf<R>()
    cursor.moveToFirst()
    while(!cursor.isAfterLast) {
        result.add(transform(cursor))
        cursor.moveToNext()
    }
    result
}

fun <R> SQLiteDatabase.queryOne(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    transform: (Cursor) -> R?
): R? = query(table, columns, selection, selectionArgs, null, null, null, "1").use { cursor ->
    if (cursor.moveToFirst()) transform(cursor) else null
}
