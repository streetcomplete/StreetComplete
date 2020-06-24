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

inline fun <reified T> Cursor.get(columnName: String): T {
    val index = getColumnIndexOrThrow(columnName)
    return when(T::class) {
        Long::class -> getLong(index)
        Int::class -> getInt(index)
        Short::class -> getShort(index)
        Double::class -> getDouble(index)
        Float::class -> getFloat(index)
        String::class -> getString(index)
        ByteArray::class -> getBlob(index)
        else -> throw ClassCastException("Expected either an Int, Short, Long, Float, Double, String or ByteArray")
    } as T
}
