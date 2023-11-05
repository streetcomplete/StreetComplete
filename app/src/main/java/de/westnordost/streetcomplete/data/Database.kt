package de.westnordost.streetcomplete.data

interface Database {

    fun exec(sql: String, args: Array<Any>? = null)

    fun <T> rawQuery(
        sql: String,
        args: Array<Any>? = null,
        transform: (CursorPosition) -> T
    ): List<T>

    fun <T> queryOne(
        table: String,
        columns: Array<String>? = null,
        where: String? = null,
        args: Array<Any>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        transform: (CursorPosition) -> T
    ): T?

    fun <T> query(
        table: String,
        columns: Array<String>? = null,
        where: String? = null,
        args: Array<Any>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: String? = null,
        distinct: Boolean = false,
        transform: (CursorPosition) -> T
    ): List<T>

    fun insert(
        table: String,
        values: Collection<Pair<String, Any?>>,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): Long

    fun insertOrIgnore(table: String, values: Collection<Pair<String, Any?>>): Long =
        insert(table, values, ConflictAlgorithm.IGNORE)

    fun replace(table: String, values: Collection<Pair<String, Any?>>): Long =
        insert(table, values, ConflictAlgorithm.REPLACE)

    fun insertMany(
        table: String,
        columnNames: Array<String>,
        valuesList: Iterable<Array<Any?>>,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): List<Long>

    fun insertOrIgnoreMany(table: String, columnNames: Array<String>, valuesList: Iterable<Array<Any?>>) =
        insertMany(table, columnNames, valuesList, ConflictAlgorithm.IGNORE)

    fun replaceMany(table: String, columnNames: Array<String>, valuesList: Iterable<Array<Any?>>) =
        insertMany(table, columnNames, valuesList, ConflictAlgorithm.REPLACE)

    fun update(
        table: String,
        values: Collection<Pair<String, Any?>>,
        where: String? = null,
        args: Array<Any>? = null,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): Int

    fun delete(
        table: String,
        where: String? = null,
        args: Array<Any>? = null
    ): Int

    fun countAll(
        table: String
    ): Long

    fun <T> transaction(block: () -> T): T
}

enum class ConflictAlgorithm {
    ROLLBACK,
    ABORT,
    FAIL,
    IGNORE,
    REPLACE
}

/** Data available at the current cursor position */
interface CursorPosition {
    /* It would be really nice if the interface would be just
       operator fun <T> get(columnName: String): T
       if T is one of the below types. But this is not possible right now in Kotlin AFAIK */
    fun getShort(columnName: String): Short
    fun getInt(columnName: String): Int
    fun getLong(columnName: String): Long
    fun getDouble(columnName: String): Double
    fun getFloat(columnName: String): Float
    fun getBlob(columnName: String): ByteArray
    fun getString(columnName: String): String
    fun getShortOrNull(columnName: String): Short?
    fun getIntOrNull(columnName: String): Int?
    fun getLongOrNull(columnName: String): Long?
    fun getDoubleOrNull(columnName: String): Double?
    fun getFloatOrNull(columnName: String): Float?
    fun getBlobOrNull(columnName: String): ByteArray?
    fun getStringOrNull(columnName: String): String?
}
