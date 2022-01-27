package de.westnordost.streetcomplete.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT
import android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteDatabase.CONFLICT_ROLLBACK
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import de.westnordost.streetcomplete.data.ConflictAlgorithm.ABORT
import de.westnordost.streetcomplete.data.ConflictAlgorithm.FAIL
import de.westnordost.streetcomplete.data.ConflictAlgorithm.IGNORE
import de.westnordost.streetcomplete.data.ConflictAlgorithm.REPLACE
import de.westnordost.streetcomplete.data.ConflictAlgorithm.ROLLBACK
import de.westnordost.streetcomplete.ktx.getBlob
import de.westnordost.streetcomplete.ktx.getBlobOrNull
import de.westnordost.streetcomplete.ktx.getDouble
import de.westnordost.streetcomplete.ktx.getDoubleOrNull
import de.westnordost.streetcomplete.ktx.getFloat
import de.westnordost.streetcomplete.ktx.getFloatOrNull
import de.westnordost.streetcomplete.ktx.getInt
import de.westnordost.streetcomplete.ktx.getIntOrNull
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getLongOrNull
import de.westnordost.streetcomplete.ktx.getShort
import de.westnordost.streetcomplete.ktx.getShortOrNull
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.getStringOrNull
import javax.inject.Inject

@SuppressLint("Recycle")
class AndroidDatabase @Inject constructor(private val dbHelper: SQLiteOpenHelper) : Database {
    private val db get() = dbHelper.writableDatabase

    override fun exec(sql: String, args: Array<Any>?) {
        if (args == null) db.execSQL(sql) else db.execSQL(sql, args)
    }

    override fun <T> rawQuery(
        sql: String,
        args: Array<Any>?,
        transform: (CursorPosition) -> T
    ): List<T> {
        val strArgs = args?.primitivesArrayToStringArray()
        return db.rawQuery(sql, strArgs).toSequence(transform).toList()
    }

    override fun <T> queryOne(
        table: String,
        columns: Array<String>?,
        where: String?,
        args: Array<Any>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        transform: (CursorPosition) -> T
    ): T? {
        val strArgs = args?.primitivesArrayToStringArray()
        return db.query(false, table, columns, where, strArgs, groupBy, having, orderBy, "1").toSequence(transform).firstOrNull()
    }

    override fun <T> query(
        table: String,
        columns: Array<String>?,
        where: String?,
        args: Array<Any>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?,
        distinct: Boolean,
        transform: (CursorPosition) -> T
    ): List<T> {
        val strArgs = args?.primitivesArrayToStringArray()
        return db.query(false, table, columns, where, strArgs, groupBy, having, orderBy, limit).toSequence(transform).toList()
    }

    override fun insert(
        table: String,
        values: Collection<Pair<String, Any?>>,
        conflictAlgorithm: ConflictAlgorithm?
    ): Long {
        return db.insertWithOnConflict(
            table,
            null,
            values.toContentValues(),
            conflictAlgorithm.toConstant()
        )
    }

    override fun insertMany(
        table: String,
        columnNames: Array<String>,
        valuesList: Iterable<Array<Any?>>,
        conflictAlgorithm: ConflictAlgorithm?
    ): List<Long> {
        val conflictStr = conflictAlgorithm.toSQL()
        val columnNamesStr = columnNames.joinToString(",")
        val placeholdersStr = Array(columnNames.size) { "?" }.joinToString(",")
        val stmt = db.compileStatement("INSERT $conflictStr INTO $table ($columnNamesStr) VALUES ($placeholdersStr)")
        val result = ArrayList<Long>()
        transaction {
            for (values in valuesList) {
                require(values.size == columnNames.size)
                for ((i, value) in values.withIndex()) {
                    // Android SQLiteProgram.bind* indices are 1-based
                    stmt.bind(i+1, value)
                }
                val rowId = stmt.executeInsert()
                result.add(rowId)
                stmt.clearBindings()
            }
            stmt.close()
        }
        return result
    }

    override fun update(
        table: String,
        values: Collection<Pair<String, Any?>>,
        where: String?,
        args: Array<Any>?,
        conflictAlgorithm: ConflictAlgorithm?
    ): Int {
        return db.updateWithOnConflict(
            table,
            values.toContentValues(),
            where,
            args?.primitivesArrayToStringArray(),
            conflictAlgorithm.toConstant()
        )
    }


    override fun delete(table: String, where: String?, args: Array<Any>?): Int {
        val strArgs = args?.primitivesArrayToStringArray()
        return db.delete(table, where, strArgs)
    }

    override fun <T> transaction(block: () -> T): T {
        db.beginTransaction()
        try {
            val result = block()
            db.setTransactionSuccessful()
            return result
        } finally {
            db.endTransaction()
        }
    }
}

private fun Array<Any>.primitivesArrayToStringArray() = Array(size) { i ->
    primitiveToString(this[i])
}

private fun primitiveToString(any: Any): String = when (any) {
    is Short, is Int, is Long, is Float, is Double -> any.toString()
    is String -> any
    else -> throw IllegalArgumentException("Cannot bind $any: Must be either Int, Long, Float, Double or String")
}

private inline fun <T> Cursor.toSequence(crossinline transform: (CursorPosition) -> T): List<T> = use { cursor ->
    val c = AndroidCursorPosition(cursor)
    cursor.moveToFirst()
    val result = ArrayList<T>(cursor.count)
    while(!cursor.isAfterLast) {
        result.add(transform(c))
        cursor.moveToNext()
    }
    return result
}

class AndroidCursorPosition(private val cursor: Cursor): CursorPosition {
    override fun getShort(columnName: String): Short = cursor.getShort(columnName)
    override fun getInt(columnName: String): Int = cursor.getInt(columnName)
    override fun getLong(columnName: String): Long = cursor.getLong(columnName)
    override fun getDouble(columnName: String): Double = cursor.getDouble(columnName)
    override fun getFloat(columnName: String): Float = cursor.getFloat(columnName)
    override fun getBlob(columnName: String): ByteArray = cursor.getBlob(columnName)
    override fun getString(columnName: String): String = cursor.getString(columnName)
    override fun getShortOrNull(columnName: String): Short? = cursor.getShortOrNull(columnName)
    override fun getIntOrNull(columnName: String): Int? = cursor.getIntOrNull(columnName)
    override fun getLongOrNull(columnName: String): Long? = cursor.getLongOrNull(columnName)
    override fun getDoubleOrNull(columnName: String): Double? = cursor.getDoubleOrNull(columnName)
    override fun getFloatOrNull(columnName: String): Float? = cursor.getFloatOrNull(columnName)
    override fun getBlobOrNull(columnName: String): ByteArray? = cursor.getBlobOrNull(columnName)
    override fun getStringOrNull(columnName: String): String? = cursor.getStringOrNull(columnName)
}

private fun Collection<Pair<String, Any?>>.toContentValues() = ContentValues(size).also {
    for ((key, value) in this) {
        when (value) {
            null -> it.putNull(key)
            is String -> it.put(key, value)
            is Short -> it.put(key, value)
            is Int -> it.put(key, value)
            is Long -> it.put(key, value)
            is Float -> it.put(key, value)
            is Double -> it.put(key, value)
            is ByteArray -> it.put(key, value)
            else -> {
                val valueType = value.javaClass.canonicalName
                throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
            }
        }
    }
}

private fun ConflictAlgorithm?.toConstant() = when(this) {
    ROLLBACK -> CONFLICT_ROLLBACK
    ABORT -> CONFLICT_ABORT
    FAIL -> CONFLICT_FAIL
    IGNORE -> CONFLICT_IGNORE
    REPLACE -> CONFLICT_REPLACE
    null -> CONFLICT_NONE
}

private fun ConflictAlgorithm?.toSQL() = when(this) {
    ROLLBACK -> " OR ROLLBACK "
    ABORT -> " OR ABORT "
    FAIL -> " OR FAIL "
    IGNORE -> " OR IGNORE "
    REPLACE -> " OR REPLACE "
    null -> ""
}

private fun SQLiteStatement.bind(i: Int, value: Any?) {
    when(value) {
        null -> bindNull(i)
        is String -> bindString(i, value)
        is Double -> bindDouble(i, value)
        is Long -> bindLong(i, value)
        is ByteArray -> bindBlob(i, value)
        is Int -> bindLong(i, value.toLong())
        is Short -> bindLong(i, value.toLong())
        is Float -> bindDouble(i, value.toDouble())
        else -> {
            val valueType = value.javaClass.canonicalName
            throw IllegalArgumentException("Illegal value type $valueType at column $i")
        }
    }
}
