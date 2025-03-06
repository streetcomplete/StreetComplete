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
        limit: Int? = null,
        distinct: Boolean = false,
        transform: (CursorPosition) -> T
    ): List<T>

    /** @return Inserted row ID */
    fun insert(
        table: String,
        values: Collection<Pair<String, Any?>>,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): Long

    /** @return Inserted row ID */
    fun insertOrIgnore(table: String, values: Collection<Pair<String, Any?>>): Long =
        insert(table, values, ConflictAlgorithm.IGNORE)

    /** @return Inserted row ID */
    fun replace(table: String, values: Collection<Pair<String, Any?>>): Long =
        insert(table, values, ConflictAlgorithm.REPLACE)

    /** @return Inserted row IDs */
    fun insertMany(
        table: String,
        columnNames: Array<String>,
        valuesList: Iterable<Array<Any?>>,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): List<Long>

    /** @return Inserted row IDs */
    fun insertOrIgnoreMany(table: String, columnNames: Array<String>, valuesList: Iterable<Array<Any?>>) =
        insertMany(table, columnNames, valuesList, ConflictAlgorithm.IGNORE)

    /** @return Inserted row IDs */
    fun replaceMany(table: String, columnNames: Array<String>, valuesList: Iterable<Array<Any?>>) =
        insertMany(table, columnNames, valuesList, ConflictAlgorithm.REPLACE)

    /** @return Number of updated rows */
    fun update(
        table: String,
        values: Collection<Pair<String, Any?>>,
        where: String? = null,
        args: Array<Any>? = null,
        conflictAlgorithm: ConflictAlgorithm? = null
    ): Int

    /** @return Number of deleted rows */
    fun delete(
        table: String,
        where: String? = null,
        args: Array<Any>? = null
    ): Int

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
    fun getInt(columnName: String): Int
    fun getLong(columnName: String): Long
    fun getDouble(columnName: String): Double
    fun getFloat(columnName: String): Float
    fun getBlob(columnName: String): ByteArray
    fun getString(columnName: String): String
    fun getIntOrNull(columnName: String): Int?
    fun getLongOrNull(columnName: String): Long?
    fun getDoubleOrNull(columnName: String): Double?
    fun getFloatOrNull(columnName: String): Float?
    fun getBlobOrNull(columnName: String): ByteArray?
    fun getStringOrNull(columnName: String): String?
}
