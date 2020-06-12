package de.westnordost.streetcomplete.ktx

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import de.westnordost.streetcomplete.data.WhereSelectionBuilder

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
    groupBy: String? = null,
    having: String? = null,
    orderBy: String? = null,
    transform: (Cursor) -> R
): List<R> = query(table, columns, selection, selectionArgs, groupBy, having, orderBy, null).use { cursor ->
    val result = ArrayList<R>(cursor.count)
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
    orderBy: String? = null,
    transform: (Cursor) -> R?
): R? = query(table, columns, selection, selectionArgs, null, null, orderBy, "1").use { cursor ->
    if (cursor.moveToFirst()) transform(cursor) else null
}

fun <R> SQLiteDatabase.query(
    table: String,
    columns: Array<String>? = null,
    selection: WhereSelectionBuilder? = null,
    groupBy: String? = null,
    having: String? = null,
    orderBy: String? = null,
    transform: (Cursor) -> R
): List<R> = query(table, columns, selection?.where, selection?.args, groupBy, having, orderBy, transform)


fun <R> SQLiteDatabase.queryOne(
    table: String,
    columns: Array<String>? = null,
    selection: WhereSelectionBuilder? = null,
    orderBy: String? = null,
    transform: (Cursor) -> R
): R? = queryOne(table, columns, selection?.where, selection?.args, orderBy, transform)


fun SQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
    val statement = compileStatement("SELECT name FROM pragma_table_info(?) WHERE name = ?")
    statement.bindString(1, tableName)
    statement.bindString(2, columnName)
    return columnName == statement.simpleQueryForString()
}
