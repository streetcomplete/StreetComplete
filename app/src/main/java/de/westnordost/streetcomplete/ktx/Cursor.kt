package de.westnordost.streetcomplete.ktx

import android.database.Cursor
import androidx.core.database.*

fun Cursor.getLong(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))
fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndexOrThrow(columnName))
fun Cursor.getShort(columnName: String): Short = getShort(getColumnIndexOrThrow(columnName))
fun Cursor.getDouble(columnName: String): Double = getDouble(getColumnIndexOrThrow(columnName))
fun Cursor.getFloat(columnName: String): Float = getFloat(getColumnIndexOrThrow(columnName))
fun Cursor.getString(columnName: String): String = getString(getColumnIndexOrThrow(columnName))
fun Cursor.getBlob(columnName: String): ByteArray = getBlob(getColumnIndexOrThrow(columnName))

fun Cursor.getLongOrNull(columnName: String): Long? = getLongOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getIntOrNull(columnName: String): Int? = getIntOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getShortOrNull(columnName: String): Short? = getShortOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getDoubleOrNull(columnName: String): Double? = getDoubleOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getFloatOrNull(columnName: String): Float? = getFloatOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getStringOrNull(columnName: String): String? = getStringOrNull(getColumnIndexOrThrow(columnName))
fun Cursor.getBlobOrNull(columnName: String): ByteArray? = getBlobOrNull(getColumnIndexOrThrow(columnName))

inline fun <R> Cursor.map(transform: (Cursor) -> R): List<R> {
    val result = mutableListOf<R>()
    moveToFirst()
    while(!isAfterLast()) {
        result.add(transform(this))
        moveToNext()
    }
    return result
}
