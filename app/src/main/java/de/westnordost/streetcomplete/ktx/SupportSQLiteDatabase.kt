package de.westnordost.streetcomplete.ktx

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder

fun <R> SupportSQLiteDatabase.queryOne(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    orderBy: String? = null,
    transform: (Cursor) -> R?
): R? {
    val query = SupportSQLiteQueryBuilder.builder(table)
        .columns(columns)
        .selection(selection, selectionArgs)
        .orderBy(orderBy)
        .limit("1")
        .create()
    return query(query).use { cursor ->
        if (cursor.moveToFirst()) transform(cursor) else null
    }
}

fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
    return queryOne(
        "pragma_table_info('$tableName')",
        arrayOf("name"),
        "name = ?",
        arrayOf(columnName)) { it.getString(0) } != null
}
